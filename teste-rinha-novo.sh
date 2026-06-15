#!/bin/bash

# Variáveis de ambiente configuráveis
export API_URL="http://localhost:9999"
export CLIENT_ID="1f41aa5a-8594-47b3-9079-9425a83fa016"
export INVALID_ID="0c2ad624-8cd8-4db7-89f6-c582cd29c5f6"

# Tokens JWT de autenticação
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0cmFuc2FjdGlvbi1jb250cm9sIiwic3ViIjoiMWY0MWFhNWEtODU5NC00N2IzLTkwNzktOTQyNWE4M2ZhMDE2Iiwicm9sZXMiOlsiQ0xJRU5UIl0sImV4cCI6MTc4MTU1MzA4NX0.oK09YPsg7mE7Czh8bQCV_bZ7QYGURaPUuKyPCqj_aII"
export INVALID_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0cmFuc2FjdGlvbi1jb250cm9sIiwic3ViIjoiMGMyYWQ2MjQtOGNkOC00ZGI3LTg5ZjYtYzU4MmNkMjljNWY2Iiwicm9sZXMiOlsiQ0xJRU5UIl0sImV4cCI6MTc4MTU1MTY0OH0.adUpp4ClEOnpPiMyVR71c-CqbWzQYzlZM8_jQ_nOV1Y"

echo "=== 1. TESTES DE ERRO E CONTRATO DE TRANSAÇÃO ==="

echo -n "Cliente Inexistente (Esperado: 404): "
curl -s -o /dev/null -w "%{http_code}\n" -X POST "$API_URL/clientes/$INVALID_ID/transacoes" \
  -H "Authorization: Bearer $INVALID_TOKEN" -H "Content-Type: application/json" \
  -d '{"valor": 100, "tipo": "c", "descricao": "valido"}'

echo -n "Sem Token JWT (Esperado: 401): "
curl -s -o /dev/null -w "%{http_code}\n" -X POST "$API_URL/clientes/$CLIENT_ID/transacoes" \
  -H "Content-Type: application/json" \
  -d '{"valor": 100, "tipo": "c", "descricao": "valido"}'

echo -n "Descrição Longa > 10 chars (Esperado: 422): "
curl -s -o /dev/null -w "%{http_code}\n" -X POST "$API_URL/clientes/$CLIENT_ID/transacoes" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"valor": 10, "tipo": "c", "descricao": "com_mais_de_dez_caracteres"}'

echo -n "Descrição Vazia (Esperado: 422): "
curl -s -o /dev/null -w "%{http_code}\n" -X POST "$API_URL/clientes/$CLIENT_ID/transacoes" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"valor": 10, "tipo": "c", "descricao": ""}'

echo -n "Tipo de transação inválido (Esperado: 422): "
curl -s -o /dev/null -w "%{http_code}\n" -X POST "$API_URL/clientes/$CLIENT_ID/transacoes" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"valor": 10, "tipo": "x", "descricao": "invalido"}'

echo -n "Valor não inteiro / Float (Esperado: 422): "
curl -s -o /dev/null -w "%{http_code}\n" -X POST "$API_URL/clientes/$CLIENT_ID/transacoes" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"valor": 1.5, "tipo": "c", "descricao": "float"}'


echo -e "\n=== 2. TESTES DE LIMITE NEGATIVO ==="

echo -n "Débito no limite exato de 10k (Esperado: 200): "
curl -s -o /dev/null -w "%{http_code}\n" -X POST "$API_URL/clientes/$CLIENT_ID/transacoes" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"valor": 10000, "tipo": "d", "descricao": "limite"}'

echo -n "Estourar limite por 1 centavo (Esperado: 422): "
curl -s -o /dev/null -w "%{http_code}\n" -X POST "$API_URL/clientes/$CLIENT_ID/transacoes" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"valor": 1, "tipo": "d", "descricao": "estouro"}'


echo -e "\n=== 3. CONCORRÊNCIA EXTREMA (Múltiplos Créditos Simultâneos) ==="
echo "Disparando 5 créditos de 20.000 em paralelo..."
for i in {1..5}; do
  curl -s -o /dev/null -w "Req $i: %{http_code}\n" -X POST "$API_URL/clientes/$CLIENT_ID/transacoes" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
    -d '{"valor": 20000, "tipo": "c", "descricao": "concorr"}' &
done; wait


echo -e "\n=== 4. CONCORRÊNCIA EXTREMA (Tentativa de burlar limite no Débito) ==="
echo "Disparando 3 débitos de 60.000 em paralelo..."
for i in {1..3}; do
  curl -s -o /dev/null -w "Req $i: %{http_code}\n" -X POST "$API_URL/clientes/$CLIENT_ID/transacoes" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
    -d '{"valor": 60000, "tipo": "d", "descricao": "concorr"}' &
done; wait
