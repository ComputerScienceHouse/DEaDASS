import React from 'react'
import { connect } from 'react-redux'
import PropTypes from 'prop-types'
import { DropdownItem, DropdownMenu, DropdownToggle, UncontrolledDropdown } from 'reactstrap'
import User from '../UserImage'

class ProfileDropdown extends React.Component {
  constructor (props) {
    super(props)

    this.toggle = this.toggle.bind(this)
    this.state = {
      isOpen: false
    }
  }

  toggle () {
    this.setState({
      isOpen: !this.state.isOpen
    })
  }

  render () {
    if (!this.props.name || !this.props.username) return null

    return (
      <UncontrolledDropdown nav inNavbar>
        <DropdownToggle nav caret className="navbar-user">
          <User {...this.props}/>
          <span className="caret"/>
        </DropdownToggle>
        <DropdownMenu>
          <DropdownItem tag="a" href="https://profiles.csh.rit.edu">
            Profile
          </DropdownItem>
          <DropdownItem tag="a" href="https://themeswitcher.csh.rit.edu">
            Theme
          </DropdownItem>
        </DropdownMenu>
      </UncontrolledDropdown>
    )
  }
}

ProfileDropdown.propTypes = {
  name: PropTypes.string,
  username: PropTypes.string
}

const mapStateToProps = state => ({
  name: ((state.oidc.user || {}).profile || {}).given_name,
  username: ((state.oidc.user || {}).profile || {}).preferred_username
})

const mapDispatchToProps = dispatch => ({
  dispatch
})

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ProfileDropdown)
