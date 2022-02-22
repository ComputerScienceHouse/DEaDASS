import PropTypes from 'prop-types'
import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faLeaf, faQuestion } from '@fortawesome/free-solid-svg-icons'

/**
 * Get a fontawesome icon to represent the given database type
 * @param {{type: string}} props type is the type to resolve an icon for
 * @returns An Icon representing this database type
 */
function DBIcon (props) {
  switch (props.type) {
    case 'mongo':
      // TODO: fontawesome doesn't have the actual mongo logo
      return (<FontAwesomeIcon title={`type: ${props.type}`} icon={faLeaf}/>)
    default:
      return (<FontAwesomeIcon title={`type: ${props.type}`} icon={faQuestion}/>)
  }
}

DBIcon.propTypes = {
  type: PropTypes.string
}

export default DBIcon
