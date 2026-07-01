-- public.currencies definition

-- Drop table

-- DROP TABLE public.currencies;
CREATE SEQUENCE IF NOT EXISTS currency_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE public.currencies (
                                   average_rate numeric(38, 2) NULL,
                                   publication_date date NULL,
                                   currency_id int8 NOT NULL,
                                   currency_code varchar(255) NOT NULL,
                                   currency_symbol varchar(255) NULL,
                                   CONSTRAINT currencies_currency_code_key UNIQUE (currency_code),
                                   CONSTRAINT currencies_pkey PRIMARY KEY (currency_id)
);


-- public.facilities definition

-- Drop table

-- DROP TABLE public.facilities;
CREATE SEQUENCE IF NOT EXISTS facility_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE public.facilities (
                                   facility_id int8 NOT NULL,
                                   description varchar(1024) NULL,
                                   city varchar(255) NULL,
                                   country varchar(255) NULL,
                                   facility_type varchar(255) NULL,
                                   "name" varchar(255) NULL,
                                   postal_code varchar(255) NULL,
                                   street varchar(255) NULL,
                                   amenities _varchar NULL,
                                   CONSTRAINT facilities_facility_type_check CHECK (((facility_type)::text = ANY ((ARRAY['HOTEL'::character varying, 'APARTMENT'::character varying])::text[]))),
	CONSTRAINT facilities_pkey PRIMARY KEY (facility_id)
);


-- public.rooms definition

-- Drop table

-- DROP TABLE public.rooms;
CREATE SEQUENCE IF NOT EXISTS room_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE public.rooms (
                              price_per_night numeric(38, 2) NULL,
                              room_capacity int4 NOT NULL,
                              facility_id int8 NULL,
                              room_id int8 NOT NULL,
                              room_number int8 NOT NULL,
                              standard_type varchar(255) NULL,
                              CONSTRAINT rooms_pkey PRIMARY KEY (room_id),
                              CONSTRAINT rooms_standard_type_check CHECK (((standard_type)::text = ANY ((ARRAY['NORMAL'::character varying, 'PREMIUM'::character varying])::text[]))),
	CONSTRAINT fkrx0d6w9p97e9cn08hdcs7a7i6 FOREIGN KEY (facility_id) REFERENCES public.facilities(facility_id)
);


-- public.users definition

-- Drop table

-- DROP TABLE public.users;
CREATE SEQUENCE IF NOT EXISTS user_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE public.users (
                              facility_id int8 NULL,
                              user_id int8 NOT NULL,
                              email varchar(255) NOT NULL,
                              "password" varchar(255) NULL,
                              phone_number varchar(255) NULL,
                              "role" varchar(255) NULL,
                              CONSTRAINT users_email_key UNIQUE (email),
                              CONSTRAINT users_pkey PRIMARY KEY (user_id),
                              CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ROLE_GUEST'::character varying, 'ROLE_MANAGER'::character varying, 'ROLE_ADMIN'::character varying])::text[]))),
	CONSTRAINT fkpl5qpsn7qmnvp22slbem3lvu6 FOREIGN KEY (facility_id) REFERENCES public.facilities(facility_id)
);


-- public.reservations definition

-- Drop table

-- DROP TABLE public.reservations;
CREATE SEQUENCE IF NOT EXISTS reservation_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE public.reservations (
                                     check_in date NULL,
                                     check_out date NULL,
                                     created_at date NULL,
                                     full_price numeric(38, 2) NULL,
                                     reservation_id int8 NOT NULL,
                                     room_id int8 NULL,
                                     user_id int8 NULL,
                                     status varchar(255) NULL,
                                     CONSTRAINT reservations_pkey PRIMARY KEY (reservation_id),
                                     CONSTRAINT reservations_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'CONFIRMED'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[]))),
	CONSTRAINT fkb5g9io5h54iwl2inkno50ppln FOREIGN KEY (user_id) REFERENCES public.users(user_id),
	CONSTRAINT fkljt6q1tp205b0h26eiegc5mx6 FOREIGN KEY (room_id) REFERENCES public.rooms(room_id)
);


-- public.room_availability definition

-- Drop table

-- DROP TABLE public.room_availability;
CREATE SEQUENCE IF NOT EXISTS room_availability_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE public.room_availability (
                                          "date" date NULL,
                                          free_slots int4 NOT NULL,
                                          room_availability_id int8 NOT NULL,
                                          room_id int8 NULL,
                                          "version" int8 NULL,
                                          CONSTRAINT room_availability_pkey PRIMARY KEY (room_availability_id),
                                          CONSTRAINT fk6amy5j70qonexbd2imncdqt5w FOREIGN KEY (room_id) REFERENCES public.rooms(room_id)
);