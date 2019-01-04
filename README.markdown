# Design and prototype a migrator

Migrator means a component, which transforms a database from a state to another. As an example, you have recently implemented a feature to send email to the users of your system. For that, you have added column `email` to `users` table. When you deploy the recent set of changes to your production environment, you want the prodution database schema also to reflect this recent recent change. For that, you need a mechanism to automatically update the schema to the new state, i.e. a migrator.

## Background

Initially you have a database containing a users table with username and password columns:

|name|type|
|----|----|
|username|varchar 100|
|password|varchar 100|

Now you want to add third column `email` to the table:

|name|type|
|----|----|
|username|varchar 100|
|password|varchar 100|
|email|varchar 100|

In it's simplest form you could have just an SQL script that could be executed:

```sql
ALTER TABLE users ADD COLUMN email VARCHAR(100);
```

However, this is suboptimal for various reasons. *First*, you are bound to one database implementation. If you would decide to e.g. switch to some other database vendor, or even change from SQL database to NoSQL database, this wouldn't work. *Second*, there can be dynamic migrations, i.e. migrations that can't be expressed with a static text file. For instance, a bit contrived example might be, that add a column to all tables which start with letter `u` and end with `s`, adding the column to `users`, `ubers` but not to `users_backup` or `employees`.

Optimally when you run the migrator, it will execute a set of scripts. They in turn migrate the state to another, without requiring any user action.
It's also important that this command is run also when you are deploying your changes to production, migrating the production database to the correct state.

Worth noting is that there can be two kinds of migrations: schema and data migrations. In short, schema migrations refer to changes to the schema, i.e. adding a new column or changing it's type. Data migrations refer to migrations that do not change the schema itself, but the data itself. I.e. change a column's contents to lowercase or increment integer column by one.

## The objective of this task

The aim of this task is to build a prototype of a migrator. For this you need to implement a tool which is able to change database schema by executing one or more migration files. How the files actually look like, or how they are implemented, is up to you.

You only need to migrate database schema in the context of this task. However, if you want you can also add an interface and corresponding migration functionality for data migrations.

This package bundles an existing low level API for accessing our "custom" database. You should not make any assumptions on it - i.e. even though it is currently SQL based, you can't assume it will be in the future.  The database nor its API are not meant to be production quality by any means. The API instead just provides a minimal simple interface for this task.

The task assignment is quite loose on purpose. Basically we just want to see a working migrator and all the rest are up to you. How you want to design it, how you want to write the migrations, and which tools and or programming languages you might want to use are your decisions.

The outcome of this task should be a working schema migrator module. We also kindly ask you to provide a short document with your implementation. This document should tell us the reasoning behind your implementation and how you ended up with that. We also appreciate your thoughts on the pros and cons on your implementation and future areas of improvements.

## Specification and restrictions

* There can be multiple migration files
* Each logical migration is stored in individual files
* User should be able to create migrations from command line e.g. via gradle. See `gradle create`
* User should be able to run migrations from command line e.g. via gradle. See `gradle migrate`
* There is no restriction on what the migration file should look like. It could be Java based DSL or even text based file
* Pre-existing tests must pass (`gradle test`)
* You can not directly interface with the underlying database. You need to use the API provided by Database API
* You may improve the API interface of the database
* In migrations, you should be able to dynamically choose the affected tables. E.g. _add timestamp to all tables in currently in database, excluding the ones already including it_
* You may use any additional libraries you see fit (excluding frameworks specifically designed for database migrations)
* You can also freely edit provided gradle scripts

## Tips & Tricks

* Good test coverage is appreciated
* Fluent interface for writing the migrations is appreciated. Think about how you would you like to write migrations?
* The command line tools do not need to be very robust
