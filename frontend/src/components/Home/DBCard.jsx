import React from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDatabase } from '@fortawesome/free-solid-svg-icons'
import { Card, CardBody, CardHeader, CardTitle } from 'reactstrap'

class DBCard extends React.Component {
  render () {
    const { server, type, name } = this.props

    return (
      <Card>
        <CardHeader>
          <FontAwesomeIcon icon={faDatabase} /> {`${server}:${type}`}
        </CardHeader>
        <CardBody>
          <Link to={`/db/${server}/${name}`}>
            <CardTitle>{name}</CardTitle>
          </Link>
        </CardBody>
      </Card>
    )
  }
}

DBCard.propTypes = {
  server: PropTypes.string,
  type: PropTypes.string,
  name: PropTypes.string,
  users: PropTypes.array
}

export default DBCard
