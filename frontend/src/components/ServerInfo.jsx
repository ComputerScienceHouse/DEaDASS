import React, { Component } from 'react'
import PropTypes from 'prop-types'
// import { Card, CardBody, CardDeck, CardTitle, Table } from 'reactstrap'
// import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
// import { faDatabase, faUser } from '@fortawesome/free-solid-svg-icons'
// import { Link } from 'react-router-dom'
import { connect } from 'react-redux'

import { GET } from '../actions/get'
// import DBIcon from './DBIcon'
import InfoSpinner from './InfoSpinner'
import DBCard from './Home/DBCard'
import ServerIcon from './ServerIcon'
import { Badge, Card, CardBody, CardDeck, CardTitle } from 'reactstrap'

function Connected (props) {
  if (props.is_connected) {
    return (<div className={props.className}><Badge color="success" pill>connected</Badge></div>)
  } else {
    return (<div className={props.className}><Badge color="warning" pill>not connected</Badge></div>)
  }
}

class ServerInfo extends Component {
  constructor (props) {
    super(props)

    this.state = {
      dbs: null,
      server: null
    }
  }

  componentDidMount () {
    // Fetch the database from the api on mount
    GET(this.props.oidc.user.access_token, `/servers/${this.props.match.params.server}/databases`)
      .then(response => response.json())
      .then(json => { console.log(json); return json })
      .then(json => { this.setState({dbs: json}) })
    GET(this.props.oidc.user.access_token, `/servers/${this.props.match.params.server}`)
      .then(response => response.json())
      .then(json => { console.log(json); return json })
      .then(json => { this.setState({server: json}) })
  }

  render () {
    if (!this.state.dbs || !this.state.server) {
      return (<InfoSpinner>Still Loading</InfoSpinner>)
    } else {
      return (
        <div>
          <Card body className="bg-transparent shadow-none" outline color="primary">
            <CardTitle className="d-flex">
              <ServerIcon className="mr-1" type={this.state.server.type} />
              {this.state.server.server}
              <Connected className="ml-auto" is_connected={this.state.server.is_connected}/>
            </CardTitle>
            <CardBody>
              <CardDeck>
                {this.state.dbs.map((db, idx) => {
                  return (<DBCard key={idx} {...db}/>)
                })}
              </CardDeck>
            </CardBody>
          </Card>
        </div>
      )
      // TODO stuff
    }
  }
}

ServerInfo.propTypes = {
  match: PropTypes.shape({
    params: PropTypes.shape({
      server: PropTypes.string
    })
  }),
  dbs: PropTypes.array, // TODO typing
  server: PropTypes.any, // TODO typing
  oidc: PropTypes.any // TODO typing
}

const mapStateToProps = (state) => {
  return {
    oidc: state.oidc
  }
}

export default connect(mapStateToProps)(ServerInfo)
