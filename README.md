# BoardNest | Boarding House Management System

A Java OOP capstone MVP built with Java 21, Spring Boot, Spring Security, Thymeleaf, Spring Data JPA, Maven, and MySQL.

## Included in this build

- Account registration and login with BCrypt password hashing
- Logout and protected application pages
- Multiple properties per account, with owner checks on property operations
- Floors and rooms with configurable capacity
- Property-scoped tenant records
- Tenant room assignment, movement, and unassignment
- Server-side capacity and ownership validation
- Responsive cream-and-forest-green interface with drag-and-drop room assignment and form-based fallback

Payments, maintenance, announcements, assignment history, and destructive delete actions are outside this first MVP.

## Requirements

- JDK 21
- Laragon / MySQL 8.x
- Internet on first Maven Wrapper run to download Maven dependencies

## 1. Create the database tables (one-time setup)

The application intentionally does not create or alter database tables automatically. The SQL script uses `CREATE TABLE IF NOT EXISTS` and does not drop existing tables. Your database `boarding_house_db` should already exist.

In VS Code PowerShell, log in to the application database account:

```powershell
mysql -h 127.0.0.1 -P 3306 -u boarding_house_app -p boarding_house_db
```

Enter that account's password. At the `mysql>` prompt, run these commands. Replace the example project path with the actual location of your project folder.

Please, for the love of god, replace the path below
with the actual location of your project folder.

```sql
source C:/path/to/BoardingHouseManagement/src/main/resources/db/manual/schema.sql;
SHOW TABLES;
```

You should see `users`, `properties`, `floors`, `rooms`, `tenants`, and `occupancies`. Then enter `exit`.

## 2. Run the application on Windows

From the project root in the same VS Code PowerShell terminal, set the database environment variables. These settings apply to that terminal session only.

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/boarding_house_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "boarding_house_app"
$env:DB_PASSWORD = "YOUR_LOCAL_APP_PASSWORD"
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Replace `YOUR_LOCAL_APP_PASSWORD` with the password you chose for the MySQL application account. Do not commit database passwords to source control. A simple password such as `1234` is suitable only for a disposable local development database, not production.

Open [http://localhost:8080](http://localhost:8080), choose **Create account**, register an application account, and sign in. The application account is separate from your MySQL account.

## 3. MVP test checklist

1. Register a new application account and sign in.
2. Create a property.
3. Add a floor and at least one room with capacity 1 or 2.
4. Add two tenants.
5. Assign one tenant to the room, then try assigning another to a full room.
6. Move or unassign a tenant and refresh the page to confirm the data persists.
7. Register a second account and confirm it cannot see the first account's properties.

## Notes

- `spring.jpa.hibernate.ddl-auto=none` and `spring.sql.init.mode=never` are deliberate safety settings.
- Do not drop or reset the database to troubleshoot login issues.
- This is a local capstone MVP; review deployment security, backups, and operational configuration before production use.
