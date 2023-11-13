--
-- PostgreSQL database dump
--

-- Dumped from database version 12.9
-- Dumped by pg_dump version 12.10

-- Started on 2023-07-06 10:01:26 UTC

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


CREATE TABLE public.t_codeable_concept (
    t_codice character varying(60) NOT NULL,
    t_descrizione character varying(2000) NOT NULL
);

ALTER TABLE public.t_codeable_concept OWNER TO kylin;

