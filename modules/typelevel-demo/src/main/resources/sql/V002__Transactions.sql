CREATE TYPE "tx_action" AS ENUM (
    'Add',
    'Remove'
    );

CREATE TABLE "point_transactions"
(
    "id"         UUID        NOT NULL DEFAULT gen_random_uuid PRIMARY KEY,
    "person_id"  UUID        NOT NULL,
    "points"     BIGINT      NOT NULL,
    "action"     tx_action   NOT NULL,
    "created_on" TIMESTAMPTZ NOT NULL default current_timestamp,
    FOREIGN KEY (person) REFERENCES people (id)
)

CREATE INDEX "point_transactions_created_on_idx" ON point_transactions (created_on);
CREATE INDEX "point_transactions_totals_idx" ON point_transactions (person, action, created_on);
CREATE INDEX "point_transactions_signed_idx"
    ON point_transactions ((CASE WHEN "action" = 'Add' THEN points ELSE -points END) );