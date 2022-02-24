import React, { Fragment } from 'react'
import PropTypes from 'prop-types'

export default function User (props) {
  const {name, username} = props

  let userStr
  // we might not have a name in some contexts (i.e. usernames not retrieved
  // from oidc)
  if (name) {
    userStr = `${name} (${username})`
  } else {
    userStr = username
  }

  return (
    <Fragment>
      <img
        className="rounded-circle"
        src={`https://profiles.csh.rit.edu/image/${username}`}
        alt=""
        aria-hidden={true}
        width={32}
        height={32}
      />
      {userStr}
    </Fragment>
  )
}

User.propTypes = {
  name: PropTypes.string,
  username: PropTypes.string
}
