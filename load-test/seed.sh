#!/usr/bin/env bash
set -euo pipefail

BASE="http://localhost:8081"

echo "==> Registering demo user..."
REGISTER=$(curl -s -X POST "$BASE/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@loadtest.com","password":"password123","firstName":"Demo","lastName":"User","role":"CUSTOMER"}')

if echo "$REGISTER" | grep -q "409\|already"; then
  echo "    User exists, logging in..."
  REGISTER=$(curl -s -X POST "$BASE/api/v1/users/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"demo@loadtest.com","password":"password123","role":"CUSTOMER"}')
fi

TOKEN=$(echo "$REGISTER" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
  echo "ERROR: Could not get token. Response: $REGISTER"
  exit 1
fi
echo "    Token obtained."

create_product() {
  local SKU=$1 NAME=$2 BRAND=$3 CATEGORY=$4 PRICE=$5 DESC=$6
  RESP=$(curl -s -X POST "$BASE/api/v1/products" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"sku\":\"$SKU\",\"name\":\"$NAME\",\"brand\":\"$BRAND\",\"category\":\"$CATEGORY\",\"price\":$PRICE,\"currency\":\"USD\",\"description\":\"$DESC\",\"stock\":500}" \
    2>/dev/null || true)
  # return existing id on conflict, or newly created id
  echo "$RESP" | grep -o '"productId":"[^"]*"' | cut -d'"' -f4 || true
}

create_inventory() {
  local PRODUCT_ID=$1 QTY=$2
  curl -s -X POST "$BASE/api/v1/inventories" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"productId\":\"$PRODUCT_ID\",\"quantity\":$QTY}" \
    > /dev/null 2>&1 || true
}

echo "==> Creating products..."
P1=$(create_product "LAPTOP-PRO-001"   "Pro Laptop 15"       "TechBrand" "electronics" 1299.99 "High-performance laptop")
P2=$(create_product "HEADPHONES-001"   "Wireless Headphones" "AudioCo"   "electronics"  199.99 "Noise-cancelling headphones")
P3=$(create_product "KEYBOARD-MECH-01" "Mechanical Keyboard" "KeyMaster" "electronics"   89.99 "RGB mechanical keyboard")
P4=$(create_product "MONITOR-4K-001"   "4K Monitor 27in"     "ViewTech"  "electronics"  449.99 "Ultra-sharp 4K display")
P5=$(create_product "MOUSE-GAMING-01"  "Gaming Mouse"        "GameGear"  "electronics"   59.99 "High-DPI gaming mouse")
echo "    Products: [$P1] [$P2] [$P3] [$P4] [$P5]"

echo "==> Seeding inventory..."
for ID in $P1 $P2 $P3 $P4 $P5; do
  [ -n "$ID" ] && create_inventory "$ID" 1000
done
echo "    Inventory seeded."

cat > load-test/.env.json <<EOF
{
  "token": "$TOKEN",
  "skus": ["LAPTOP-PRO-001","HEADPHONES-001","KEYBOARD-MECH-01","MONITOR-4K-001","MOUSE-GAMING-01"],
  "productIds": ["$P1","$P2","$P3","$P4","$P5"]
}
EOF

echo ""
echo "==> Seed complete. Token and product IDs written to load-test/.env.json"
