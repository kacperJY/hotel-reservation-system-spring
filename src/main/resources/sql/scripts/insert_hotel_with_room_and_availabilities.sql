insert into public.facilities (facility_id,
                               description,
                               city,
                               country,
                               facility_type,
                               name,
                               postal_code,
                               street,
                               amenities)
values (1,
        'Luksusowy hotel w samym sercu Warszawy z widokiem na Pałac Kultury.',
        'Warszawa',
        'Poland',
        'HOTEL',
        'Grand Central Hotel',
        '00-001',
        'ul. Marszałkowska 10',
        ARRAY['WiFi', 'Basen', 'SPA', 'Restauracja', 'Parking']::varchar[]);



insert into public.rooms (room_id,
                          room_number,
                          price_per_night,
                          room_capacity,
                          standard_type,
                          facility_id)
values (1,
        101,
        450.00,
        2,
        'NORMAL',
        1);


INSERT INTO public.room_availability (
    room_availability_id,
    room_id,
    "date",
    free_slots,
    "version"
)

SELECT
    nextval('room_availability_seq') + i,
    1,
    '2026-07-01'::date + i,
    1,
    0
FROM generate_series(0,30) AS i;