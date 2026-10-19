# BoardNest | Boarding House Management System

A Java OOP capstone MVP built with Java 21, Spring Boot, Spring Security, Thymeleaf, Spring Data JPA, Maven, and MySQL.

## Included in this build

- Account registration and login with BCrypt password hashing
- Logout and protected application pages
- Multiple properties per account, with owner checks on property operations
- Floors and rooms with configurable capacity
- Property-scoped tenant records
- Tenant room assignment, movement, and unassignment
- Tenant editing and deletion with confirmation
- Per-tenant monthly rent tracking
- Server-side capacity and ownership validation
- Responsive cream-and-forest-green interface with drag-and-drop room assignment and form-based fallback

Payment collection/history, maintenance, announcements, and assignment history remain outside this MVP.

## Requirements

- JDK 21
- Laragon / MySQL 8.x
- Internet on first Maven Wrapper run to download Maven dependencies

## 1. First-time setup and normal startup (Windows)

1. Install JDK 21 and MySQL (Laragon is supported). The launcher cannot install these prerequisites for you.
2. Double-click `Setup-BoardNest.bat` to review prerequisite guidance.
3. Double-click `Start-BoardNest.bat`.
4. Enter the application database username (default `boarding_house_app`) and a password. If the account/database is missing or the app account cannot log in, the launcher asks for a MySQL administrator username (default `root`) and password.
5. With administrator permission, the launcher creates `boarding_house_db`, creates/updates the application account, grants it access to this database, applies the idempotent schema, checks the six required tables, and applies the monthly-rent migration only if that column is missing.
6. The launcher starts the application and opens `http://localhost:8080` when it responds.

The launcher searches for `mysql.exe` on PATH and in common Laragon/MySQL install locations. If it cannot find the client, install/use Laragon or add the MySQL `bin` folder to PATH. It attempts to start a detected Windows MySQL/MariaDB service or Laragon, but unusual installations may need manual startup. It never saves database passwords into project files. Existing tables are not dropped or reset.

## 2. Existing database upgrade

If you already created the database before monthly rent was added, run the migration SQL **once** at the MySQL prompt using a path relative to your own project folder:

```sql
source C:/path/to/BoardNest/src/main/resources/db/manual/migration_add_tenant_monthly_rent.sql;
```

New databases get the `monthly_rent` column from `schema.sql` automatically.

## 3. Run the application on Windows

For normal startup, double-click `Start-BoardNest.bat`. It verifies project files and Java 21, checks MySQL reachability, attempts to start a detected service or Laragon, verifies/creates the database and account when needed, checks tables and applies the monthly-rent migration if missing, then runs the Maven Wrapper. Maven downloads dependencies on first run.

Alternatively, from the project root in PowerShell, set the database environment variables. These settings apply to that terminal session only.

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/boarding_house_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "boarding_house_app"
$env:DB_PASSWORD = "YOUR_LOCAL_APP_PASSWORD"
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Replace `YOUR_LOCAL_APP_PASSWORD` with the password you chose for the MySQL application account. Do not commit database passwords to source control. A simple password such as `1234` is suitable only for a disposable local development database, not production.

Open [http://localhost:8080](http://localhost:8080), choose **Create account**, register an application account, and sign in. The application account is separate from your MySQL account.

## 4. MVP test checklist

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
