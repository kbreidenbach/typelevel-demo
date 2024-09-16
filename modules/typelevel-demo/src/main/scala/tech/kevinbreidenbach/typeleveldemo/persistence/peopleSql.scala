package tech.kevinbreidenbach.typeleveldemo.persistence

import tech.kevinbreidenbach.typeleveldemo.domain.Email
import tech.kevinbreidenbach.typeleveldemo.domain.Firstname
import tech.kevinbreidenbach.typeleveldemo.domain.ID
import tech.kevinbreidenbach.typeleveldemo.domain.Lastname
import tech.kevinbreidenbach.typeleveldemo.domain.Person
import tech.kevinbreidenbach.typeleveldemo.domain.Status

import doobie.Fragment
import doobie.implicits.given

//format: off
//noinspection SqlNoDataSourceInspection
val upsertPersonSql: Person => Fragment = person =>
  sql"""INSERT INTO people (email, first_name, last_name, status)
        VALUES ( ${person.email}, ${person.firstname}, ${person.lastname}, ${Status.Created} )
        ON CONFLICT(email) DO UPDATE
          SET
            first_name = ${person.firstname},
            last_name = ${person.lastname}
            status = ${Status.Updated}
            updated_on = current_timestamp
          WHERE
            deleted_on IS NULL
        RETURNING email, first_name, last_name, status, created_on, updated_on, deleted_on;"""

//noinspection SqlNoDataSourceInspection
val findPersonByFirstnameSql: Firstname => Fragment = firstname =>
  sql"""SELECT * FROM people WHERE first_name = $firstname;"""

//noinspection SqlNoDataSourceInspection
val findPersonByLastnameSql: Lastname => Fragment = lastname =>
  sql"""SELECT * FROM people WHERE last_name = $lastname;"""

//noinspection SqlNoDataSourceInspection
val findPersonByEmailSql: Email => Fragment = email =>
  sql"""SELECT * FROM people WHERE email = $email;"""

//noinspection SqlNoDataSourceInspection
val findPersonByIdSql: ID => Fragment = id =>
  sql"""SELECT * FROM people WHERE id = $id;"""

//noinspection SqlNoDataSourceInspection
val deletePersonByEmailSql: Email => Fragment = email =>
  sql"""UPDATE people SET 
          deleted_on = current_timestamp,
          status = ${Status.Deleted}
        WHERE email = $email
        AND deleted_on IS NULL
        RETURNING email, first_name, last_name, status, created_on, updated_on, deleted_on;"""

//noinspection SqlNoDataSourceInspection
val deletePersonByIdSql: ID => Fragment = id => 
  sql"""UPDATE people SET 
          deleted_on = current_timestamp,
          status = ${Status.Deleted}
        WHERE id = $id
        AND deleted_on IS NULL
        RETURNING email, first_name, last_name, status, created_on, updated_on, deleted_on;"""
