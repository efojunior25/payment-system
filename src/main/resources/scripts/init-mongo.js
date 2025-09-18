// Inicialização do MongoDB para XunimPay
db = db.getSiblingDB('xunimpay_audit');

// Criar usuário para a aplicação
db.createUser({
  user: 'xunimpay_user',
  pwd: 'xunim123',
  roles: [
    { role: 'readWrite', db: 'xunimpay_audit' }
  ]
});

// Criar índices para performance
db.audit_logs.createIndex({ "entity_type": 1, "entity_id": 1 });
db.audit_logs.createIndex({ "timestamp": -1 });
db.audit_logs.createIndex({ "user_id": 1 });
db.audit_logs.createIndex({ "action": 1 });

// Inserir documento inicial
db.audit_logs.insertOne({
  entity_type: "SYSTEM",
  entity_id: "INIT",
  action: "CREATE",
  timestamp: new Date(),
  details: {
    description: "XunimPay System inicializado com sucesso",
    version: "1.0.0"
  }
});

print("MongoDB inicializado com sucesso para XunimPay!");