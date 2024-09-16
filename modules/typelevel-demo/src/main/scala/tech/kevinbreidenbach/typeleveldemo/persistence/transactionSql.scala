package tech.kevinbreidenbach.typeleveldemo.persistence

import tech.kevinbreidenbach.typeleveldemo.domain.IncomingTransaction
import tech.kevinbreidenbach.typeleveldemo.domain.PersonID

import doobie.Fragment
import doobie.implicits.given

//format: off
//noinspection SqlNoDataSourceInspection
val insertPointsTransactionSql: IncomingTransaction => Fragment = transaction =>
  sql"""INSERT INTO point_transactions (person_id, points, action)
        VALUES ( ${transaction.personId}, ${transaction.points}, ${transaction.action} )
        RETURNING "id", "person_id", "points", "action", "created_on";"""
  
  
//noinspection SqlNoDataSourceInspection
val findPointsTotalSql: PersonID => Fragment = personId => 
  sql"""SELECT SUM((CASE WHEN "action" = 'Add' THEN points ELSE -points END)) 
        FROM point_transaction 
        WHERE person_id = ${personId}"""  
