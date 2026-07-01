import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Rate} from 'k6/metrics';

// Load seed data written by seed.sh
const env = JSON.parse(open('.env.json'));
const TOKEN = env.token;
const SKUS = env.skus;
const PRODUCT_IDS = env.productIds;

const BASE = 'http://localhost:8081';
const AUTH = { headers: { 'Authorization': `Bearer ${TOKEN}`, 'Content-Type': 'application/json' } };

// Custom metrics visible in k6 summary
const rateLimited  = new Counter('rate_limited_429');
const orderCreated = new Counter('orders_created');
const cacheHits    = new Rate('product_read_success');

export const options = {
  scenarios: {
    // Scenario 1: steady product reads — warms Redis cache, generates hit/miss ratio
    product_reads: {
      executor: 'constant-vus',
      vus: 15,
      duration: '3m',
      tags: { scenario: 'product_reads' },
    },

    // Scenario 2: cart activity — shows cart service + Redis hash metrics
    cart_activity: {
      executor: 'constant-vus',
      vus: 5,
      duration: '3m',
      tags: { scenario: 'cart' },
      exec: 'cartFlow',
    },

    // Scenario 3: order saga — fires order.created events through Kafka
    order_saga: {
      executor: 'constant-arrival-rate',
      rate: 2,               // 2 orders/sec — enough to see Kafka topic metrics
      timeUnit: '1s',
      preAllocatedVUs: 10,
      duration: '3m',
      startTime: '20s',      // let cache warm first
      tags: { scenario: 'orders' },
      exec: 'placeOrder',
    },

    // Scenario 4: rate limit burst — generates 429s for the demo chart
    rate_limit_burst: {
      executor: 'ramping-arrival-rate',
      startTime: '90s',      // midway through, after baseline is visible
      preAllocatedVUs: 80,
      stages: [
        { target: 120, duration: '15s' },  // ramp hard — hits burst capacity
        { target: 120, duration: '30s' },  // hold — sustained 429s
        { target: 0,   duration: '15s' },  // back off — 429s stop
      ],
      tags: { scenario: 'burst' },
      exec: 'burstProducts',
    },
  },

  thresholds: {
    'rate_limited_429': [],               // just track, no pass/fail
    'http_req_duration{scenario:product_reads}': ['p(95)<500'],
    'http_req_duration{scenario:orders}':        ['p(95)<2000'],
  },
};

// ── Scenario: product reads ──────────────────────────────────────────────────
export default function () {
  const sku = SKUS[Math.floor(Math.random() * SKUS.length)];

  // Mix of by-SKU (point lookup → Redis) and by-category (Cassandra paginated)
  const coin = Math.random();
  let res;
  if (coin < 0.7) {
    res = http.get(`${BASE}/api/v1/products/${sku}`, AUTH);
  } else {
    res = http.get(`${BASE}/api/v1/products/category/electronics`, AUTH);
  }

  const ok = check(res, { '2xx': (r) => r.status >= 200 && r.status < 300 });
  cacheHits.add(ok);
  if (res.status === 429) rateLimited.add(1);

  sleep(0.2 + Math.random() * 0.3);
}

// ── Scenario: cart flow ──────────────────────────────────────────────────────
export function cartFlow() {
  const idx = Math.floor(Math.random() * PRODUCT_IDS.length);
  const productId = PRODUCT_IDS[idx];
  const sku       = SKUS[idx];

  // Add item
  http.post(`${BASE}/api/v1/cart/items`, JSON.stringify({
    productId, sku, name: 'Load Test Product', price: 99.99, quantity: 1,
  }), AUTH);

  sleep(0.5);

  // View cart
  http.get(`${BASE}/api/v1/cart`, AUTH);

  sleep(0.3);
}

// ── Scenario: order saga ─────────────────────────────────────────────────────
export function placeOrder() {
  const sku = SKUS[Math.floor(Math.random() * SKUS.length)];

  const res = http.post(`${BASE}/api/v1/orders`, JSON.stringify({
    items: [{ sku, quantity: 1 }],
  }), AUTH);

  const ok = check(res, { 'order accepted': (r) => r.status === 201 || r.status === 200 });
  if (ok) orderCreated.add(1);

  sleep(1);
}

// ── Scenario: burst (rate limit demo) ───────────────────────────────────────
export function burstProducts() {
  const sku = SKUS[Math.floor(Math.random() * SKUS.length)];
  const res = http.get(`${BASE}/api/v1/products/${sku}`, AUTH);

  if (res.status === 429) rateLimited.add(1);
  check(res, {
    '2xx or 429': (r) => r.status < 300 || r.status === 429,
  });
}
