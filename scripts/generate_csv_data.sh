#!/bin/bash
# Script pour générer les fichiers CSV nécessaires pour les tests JMeter

set -e

JMETER_DIR="jmeter/data"
mkdir -p "$JMETER_DIR"

echo "📝 Génération des fichiers CSV pour JMeter..."

# 1. Générer ids.csv avec des IDs valides
# Format: itemId,categoryId
# On génère 1000 lignes pour avoir assez de variété
echo "Génération de ids.csv..."
cat > "$JMETER_DIR/ids.csv" << 'EOF'
itemId,categoryId
EOF

# Générer 1000 paires itemId/categoryId
# Items: 1-100000, Categories: 1-2000
for i in {1..1000}; do
  item_id=$((1 + (i % 100000)))
  category_id=$((1 + (i % 2000)))
  echo "$item_id,$category_id" >> "$JMETER_DIR/ids.csv"
done

echo "✅ ids.csv généré avec $(wc -l < "$JMETER_DIR/ids.csv") lignes"

# 2. Générer payloads_1k.csv (payloads légers ~1KB)
echo "Génération de payloads_1k.csv..."
cat > "$JMETER_DIR/payloads_1k.csv" << 'EOF'
payload
EOF

# Générer 50 payloads de ~1KB chacun
for i in {1..50}; do
  sku=$(printf "SKU%06d" $i)
  name="Item Test $i"
  price=$(awk "BEGIN {printf \"%.2f\", 10 + ($i % 90)}")
  stock=$((i % 500))
  category_id=$((1 + (i % 2000)))
  
  # Créer un payload JSON avec padding pour atteindre ~1KB
  pad_length=$((1000 - 150))  # ~150 chars pour le JSON de base
  pad=$(head -c $pad_length < /dev/zero | tr '\0' 'x' | head -c $pad_length)
  
  payload="{\"sku\":\"$sku\",\"name\":\"$name\",\"price\":$price,\"stock\":$stock,\"category\":{\"id\":$category_id},\"description\":\"$pad\"}"
  echo "$payload" >> "$JMETER_DIR/payloads_1k.csv"
done

echo "✅ payloads_1k.csv généré avec $(wc -l < "$JMETER_DIR/payloads_1k.csv") lignes"

# 3. Générer payloads_5k.csv (payloads lourds ~5KB)
echo "Génération de payloads_5k.csv..."
cat > "$JMETER_DIR/payloads_5k.csv" << 'EOF'
payload
EOF

# Générer 20 payloads de ~5KB chacun
for i in {1..20}; do
  sku=$(printf "SKU%06d" $i)
  name="Item Heavy Test $i"
  price=$(awk "BEGIN {printf \"%.2f\", 10 + ($i % 90)}")
  stock=$((i % 500))
  category_id=$((1 + (i % 2000)))
  
  # Créer un payload JSON avec padding pour atteindre ~5KB
  pad_length=$((5000 - 150))  # ~150 chars pour le JSON de base
  pad=$(head -c $pad_length < /dev/zero | tr '\0' 'y' | head -c $pad_length)
  
  payload="{\"sku\":\"$sku\",\"name\":\"$name\",\"price\":$price,\"stock\":$stock,\"category\":{\"id\":$category_id},\"description\":\"$pad\"}"
  echo "$payload" >> "$JMETER_DIR/payloads_5k.csv"
done

echo "✅ payloads_5k.csv généré avec $(wc -l < "$JMETER_DIR/payloads_5k.csv") lignes"

echo ""
echo "📊 Résumé des fichiers générés :"
echo "  - ids.csv: $(wc -l < "$JMETER_DIR/ids.csv") lignes"
echo "  - payloads_1k.csv: $(wc -l < "$JMETER_DIR/payloads_1k.csv") lignes"
echo "  - payloads_5k.csv: $(wc -l < "$JMETER_DIR/payloads_5k.csv") lignes"
echo ""
echo "✅ Tous les fichiers CSV ont été générés dans $JMETER_DIR/"

