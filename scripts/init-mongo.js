// Inicialização do MongoDB
db = db.getSiblingDB('xunimpay_audit');

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
    description: "Sistema inicializado com sucesso"
  }
});

print("MongoDB inicializado com sucesso!");