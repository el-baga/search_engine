--
-- PostgreSQL database dump
--

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: site; Type: TABLE; Schema: public
--

CREATE TABLE public.site (
    id bigint NOT NULL,
    status_time timestamp(6) without time zone,
    status character varying(255),
    last_error text,
    url character varying(255),
    name character varying(255)
);

--
-- Name: site_id_seq; Type: SEQUENCE; Schema: public
--

CREATE SEQUENCE public.site_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: site_id_seq; Type: SEQUENCE OWNED BY; Schema: public
--

ALTER SEQUENCE public.site_id_seq OWNED BY public.site.id;

--
-- Name: lemma; Type: TABLE; Schema: public
--

CREATE TABLE public.lemma (
    id bigint NOT NULL,
    lemma_word character varying(255) NOT NULL,
    site_id bigint NOT NULL,
    frequency bigint NOT NULL
);


--
-- Name: lemma_id_seq; Type: SEQUENCE; Schema: public
--

CREATE SEQUENCE public.lemma_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: lemma_id_seq; Type: SEQUENCE OWNED BY; Schema: public
--

ALTER SEQUENCE public.lemma_id_seq OWNED BY public.lemma.id;


--
-- Name: page; Type: TABLE; Schema: public
--

CREATE TABLE public.page (
    id bigint NOT NULL,
    path character varying(300) NOT NULL,
    content text NOT NULL,
    code bigint NOT NULL,
    site_id bigint NOT NULL
);

--
-- Name: page_id_seq; Type: SEQUENCE; Schema: public
--

CREATE SEQUENCE public.page_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: page_id_seq; Type: SEQUENCE OWNED BY; Schema: public
--

ALTER SEQUENCE public.page_id_seq OWNED BY public.page.id;

--
-- Name: search_index; Type: TABLE; Schema: public
--

CREATE TABLE public.search_index (
    id bigint NOT NULL,
    lemma_id bigint NOT NULL,
    page_id bigint NOT NULL,
    index_rank double precision NOT NULL
);

--
-- Name: search_index_id_seq; Type: SEQUENCE; Schema: public
--

CREATE SEQUENCE public.search_index_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: search_index_id_seq; Type: SEQUENCE OWNED BY; Schema: public
--

ALTER SEQUENCE public.search_index_id_seq OWNED BY public.search_index.id;

--
-- Name: indexation_flag; Type: TABLE; Schema: public
--

CREATE TABLE public.indexation_flag (
    id bigint NOT NULL,
    is_indexation_running boolean,
    is_index_one_page_active boolean
);

--
-- Name: indexation_flag_id_seq; Type: SEQUENCE; Schema: public
--

CREATE SEQUENCE public.indexation_flag_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: indexation_flag_id_seq; Type: SEQUENCE OWNED BY; Schema: public
--

ALTER SEQUENCE public.indexation_flag_id_seq OWNED BY public.indexation_flag.id;

--
-- Name: site id; Type: DEFAULT; Schema: public
--

ALTER TABLE ONLY public.site ALTER COLUMN id SET DEFAULT nextval('public.site_id_seq'::regclass);

--
-- Name: page id; Type: DEFAULT; Schema: public
--

ALTER TABLE ONLY public.page ALTER COLUMN id SET DEFAULT nextval('public.page_id_seq'::regclass);

--
-- Name: lemma id; Type: DEFAULT; Schema: public
--

ALTER TABLE ONLY public.lemma ALTER COLUMN id SET DEFAULT nextval('public.lemma_id_seq'::regclass);

--
-- Name: search_index id; Type: DEFAULT; Schema: public
--

ALTER TABLE ONLY public.search_index ALTER COLUMN id SET DEFAULT nextval('public.search_index_id_seq'::regclass);

--
-- Name: indexation_flag id; Type: DEFAULT; Schema: public
--

ALTER TABLE ONLY public.indexation_flag ALTER COLUMN id SET DEFAULT nextval('public.indexation_flag_id_seq'::regclass);

--
-- Name: site site_pkey; Type: CONSTRAINT; Schema: public
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_pkey PRIMARY KEY (id);


--
-- Name: page page_pkey; Type: CONSTRAINT; Schema: public
--

ALTER TABLE ONLY public.page
    ADD CONSTRAINT page_pkey PRIMARY KEY (id);


--
-- Name: lemma lemma_pkey; Type: CONSTRAINT; Schema: public
--

ALTER TABLE ONLY public.lemma
    ADD CONSTRAINT lemma_pkey PRIMARY KEY (id);


--
-- Name: search_index search_index_pkey; Type: CONSTRAINT; Schema: public
--

ALTER TABLE ONLY public.search_index
    ADD CONSTRAINT search_index_pkey PRIMARY KEY (id);

--
-- Name: indexation_flag indexation_flag_pkey; Type: CONSTRAINT; Schema: public
--

ALTER TABLE ONLY public.indexation_flag
    ADD CONSTRAINT indexation_flag_pkey PRIMARY KEY (id);

--
-- Name: page_path_idx; Type: INDEX; Schema: public
--

CREATE INDEX page_path_idx ON public.page USING hash (path);


--
-- Name: page fk_page_site_id; Type: FK CONSTRAINT; Schema: public
--

ALTER TABLE ONLY public.page
    ADD CONSTRAINT fk_page_site_id FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- Name: lemma fk_lemma_ste_id; Type: FK CONSTRAINT; Schema: public
--

ALTER TABLE ONLY public.lemma
    ADD CONSTRAINT fk_lemma_ste_id FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- Name: search_index fk_search_index_page_id; Type: FK CONSTRAINT; Schema: public
--

ALTER TABLE ONLY public.search_index
    ADD CONSTRAINT fk_search_index_page_id FOREIGN KEY (page_id) REFERENCES public.page(id);


--
-- Name: search_index fk_search_index_lemma_id; Type: FK CONSTRAINT; Schema: public
--

ALTER TABLE ONLY public.search_index
    ADD CONSTRAINT fk_search_index_lemma_id FOREIGN KEY (lemma_id) REFERENCES public.lemma(id);

--
-- PostgreSQL database dump complete
--

