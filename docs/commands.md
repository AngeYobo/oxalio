# 1. Clean et installation des dépendances
mvn clean install

# 2. Générer les sources MapStruct
mvn clean compile

# 3. Lancer les tests
mvn test

# 4. Démarrer l'application
mvn spring-boot:run

# 5. Migrer la base de données
mvn flyway:migrate

# 6. Vérifier la génération du mapper
ls target/generated-sources/annotations/com/oxalio/invoiceservice/mapper/

mvn spring-boot:run -Dspring-boot.run.profiles=mock