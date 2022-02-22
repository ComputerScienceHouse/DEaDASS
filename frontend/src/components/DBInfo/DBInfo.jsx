import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Card, CardBody, CardDeck, CardHeader, Table } from 'reactstrap'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDatabase, faUser } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'

import DBIcon from '../DBIcon'

class DBInfo extends Component {
  render () {
    // todo: fetch database from backend, create a DBUserCard for each user
    // todo: create /db/$server page
    return (
      <div>
        <DatabaseCard database={{
          server: this.props.match.params.server, type: 'mongo', name: this.props.match.params.name}} />

        <CardDeck>
          <DBUserCard
            dbUser={{
              server: 'local',
              type: 'mongo',
              user: 'deadass',
              roles: [{ role: 'root', db: 'admin' }],
              extra_data: { db: 'admin' }
            }}
          />
        </CardDeck>
      </div>
    )
  }
}

DBInfo.propTypes = {
  match: PropTypes.shape({params: PropTypes.shape({server: PropTypes.string, name: PropTypes.string})})
}

export default DBInfo

function DatabaseCard (props) {
  const {server, type, name} = props.database
  return (
    <Card>
      <CardHeader>
        <FontAwesomeIcon icon={faDatabase}/> <DBIcon type={type}/> <b>{name}</b> on <Link to={`/db/${server}`}>{server}</Link>
      </CardHeader>
    </Card>
  )
}

// see Database in backend/src/db_connection.ts
DatabaseCard.propTypes = {
  database: PropTypes.shape({
    server: PropTypes.string,
    type: PropTypes.string,
    name: PropTypes.string
  })
}

function DBRoleRow (props) {
  const {role, db} = props
  return (
    <tr>
      <td>{role}</td>
      <td>{db}</td>
    </tr>
  )
}

// see DBRole in backend/src/db_connection.ts
DBRoleRow.propTypes = {
  role: PropTypes.string,
  db: PropTypes.string
}

function DBUserCard (props) {
  const { user, server, type, roles } = props.dbUser
  return (
    <Card>
      <CardHeader><FontAwesomeIcon icon={faUser}/> {user}</CardHeader>
      <CardBody>
        <Table>
          <tbody>
            <tr>
              <th scope='row'>Server</th>
              <td>{server}</td>
            </tr>
            <tr>
              <th scope='row'>Type</th>
              <td>{type}</td>
            </tr>
            <tr>
              <th scope='row'>Roles</th>
              <td>
                <Table>
                  <thead>
                    <th>Role</th>
                    <th>DB</th>
                  </thead>
                  <tbody>
                    {roles.map((role, index) => <DBRoleRow key={index} role={role.role} db={role.db}/>)}
                  </tbody></Table></td>
            </tr>
          </tbody>
        </Table>
      </CardBody>
    </Card>
  )
}

DBUserCard.propTypes = {
  dbUser: PropTypes.shape({
    user: PropTypes.string,
    server: PropTypes.string,
    type: PropTypes.string,
    roles: PropTypes.arrayOf(PropTypes.shape({role: PropTypes.string, db: PropTypes.string
    }))
  })
}
