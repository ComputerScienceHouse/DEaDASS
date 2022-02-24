import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Card, CardBody, CardDeck, CardHeader, Table } from 'reactstrap'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDatabase, faUser } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'
import { connect } from 'react-redux'

import { GET } from '../../actions/get'
import ServerIcon from '../ServerIcon'
import InfoSpinner from '../InfoSpinner'

class DBInfo extends Component {
  constructor (props) {
    super(props)

    this.state = {
      database: null
    }
  }

  componentDidMount () {
    // Fetch the database from the api on mount
    GET(this.props.oidc.user.access_token, `/databases/${this.props.match.params.server}/${this.props.match.params.name}`)
      .then(response => response.json())
      .then(json => { this.setState({database: json}) })
  }

  render () {
    if (!this.state.database) {
      return (<InfoSpinner>Still Loading</InfoSpinner>)
    } else {
      return (
        <div>
          <DatabaseCard database={this.state.database} />

          <CardDeck>
            {this.state.database.users.map((user, idx) => {
              return (<DBUserCard key={idx} dbUser={user}/>)
            })}
          </CardDeck>
        </div>
      )
    }

    // TODO: create /db/$server page
  }
}

DBInfo.propTypes = {
  match: PropTypes.shape({
    params: PropTypes.shape({
      server: PropTypes.string,
      name: PropTypes.string
    })
  }),
  database: PropTypes.object, // TODO typing
  oidc: PropTypes.any // TODO typing
}

const mapStateToProps = (state) => {
  return {
    oidc: state.oidc,
    databases: state.apis.databases
  }
}

export default connect(mapStateToProps)(DBInfo)

function DatabaseCard (props) {
  const {server, type, name} = props.database
  return (
    <Card>
      <CardHeader>
        <FontAwesomeIcon icon={faDatabase}/> <ServerIcon type={type}/> <b>{name}</b> on <Link to={`/db/${server}`}>{server}</Link>
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
    roles: PropTypes.arrayOf(
      PropTypes.shape({
        role: PropTypes.string,
        db: PropTypes.string
      })
    )
  })
}
