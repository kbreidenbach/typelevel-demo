CREATE TYPE "status" AS ENUM (
    'Created',
    'Updated',
    'Deleted'
    );

COMMENT ON TYPE status IS 'This enumeration provides values for the status of a person';

CREATE TABLE "people"
(
    "id"         UUID        NOT NULL DEFAULT gen_random_uuid PRIMARY KEY,
    "email"      TEXT        NOT NULL UNIQUE,
    "fist_name"  TEXT        NOT NULL,
    "last_name"  TEXT        NOT NULL,
    "status"     status      NOT NULL,
    "created_on" TIMESTAMPTZ NOT NULL default current_timestamp,
    "updated_on" TIMESTAMPTZ,
    "deleted_on" TIMESTAMPTZ
);

COMMENT ON COLUMN people.status IS E' ||
E''The current status of the person. "deleted" allows us to soft delete a record';

CREATE OR REPLACE FUNCTION set_updated_on()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_on = now();
    NEW.status = 'Updated';
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER set_updated_on_people_trigger
    BEFORE UPDATE
    ON people
    FOR EACH ROW
EXECUTE PROCEDURE set_updated_on();
