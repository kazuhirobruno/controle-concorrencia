#!/bin/bash

# Variáveis de ambiente configuráveis
export API_URL="http://localhost:9999"
export CLIENT_ID="9d9c23d8-c09b-4e1f-be55-230a5fb96150"
export INVALID_ID="6417973-f1bb-48ba-818f-729d70e25e07"

# Tokens JWT de autenticação
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0cmFuc2FjdGlvbi1jb250cm9sIiwic3ViIjoiOWQ5YzIzZDgtYzA5Yi00ZTFmLWJlNTUtMjMwYTVmYjk2MTUwIiwicm9sZXMiOlsiQ0xJRU5UIl0sImV4cCI6MTc4MTczMTgyMH0.PSVV4IAWQTbfd8hIMO0tz_L2XsQmMmWZXvdY8pPBLLo"
export INVALID_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0cmFuc2FjdGlvbi1jb250cm9sIiwic3ViIjoiYTY0MTc5NzMtZjFiYi00OGJhLTgxOGYtNzI5ZDcwZTI1ZTA3Iiwicm9sZXMiOlsiQ0xJRU5UIl0sImV4cCI6MTc4MTczMTc2NX0.lKY_ndBR0anvfF17Xq8gxQOX08TPoOPJ9vo4G9v03m4"

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
