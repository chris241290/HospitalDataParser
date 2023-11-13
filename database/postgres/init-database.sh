#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    create database kylin;
    create user kylin with encrypted password 'kylin';
    grant all privileges on database kylin to kylin;
EOSQL

cd /init_sql

psql -U kylin < /init_sql/db_init.sql