/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 Wal-Mart Store, Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { reset as resetForm } from 'redux-form';
import { push as pushHistory } from 'react-router-redux';
import { Header, Loader } from 'semantic-ui-react';
import ErrorMessage from '../shared/ErrorMessage';
import ProjectForm from './form';
import { actions, reducers, sagas, selectors } from './crud';
import * as repoConstants from './RepositoryPopup/constants';
import { getCurrentOrg } from '../session/reducers';

/**
 * Converts the server's representation of a project object into the form's format.
 * @param data
 * @return {*}
 */
const rawToForm = (data) => {
  if (!data.repositories) {
    return data;
  }

  const repos = [];
  for (const k in data.repositories) {
    const src = data.repositories[k];

    const dst = { name: k, ...src };
    dst.sourceType = repoConstants.BRANCH_SOURCE_TYPE;
    if (dst.commitId) {
      dst.sourceType = repoConstants.REV_SOURCE_TYPE;
    }

    repos.push(dst);
  }

  data.repositories = repos;
  return data;
};

/**
 * Converts the form's representation of a project object into the server's format.
 * @param data
 */
const formToRaw = (data) => {
  if (!data.repositories) {
    return data;
  }

  const repos = {};
  for (const src of data.repositories) {
    const dst = Object.assign({}, src);
    delete dst.name;
    repos[src.name] = dst;
  }

  data.repositories = repos;
  return data;
};

class ProjectPage extends Component {
  componentDidMount() {
    this.load();
  }

  componentDidUpdate(prevProps) {
    const { projectName: prevProjectName } = prevProps;
    const { projectName: currentProjectName } = this.props;

    if (!prevProjectName || prevProjectName !== currentProjectName) {
      this.load();
    }
  }

  load() {
    const { createNew, reset } = this.props;
    if (createNew) {
      reset();
      return;
    }

    const { loadData, projectName, org } = this.props;
    loadData(org.name, projectName);
  }

  handleSave(data) {
    const { saveData, org } = this.props;
    saveData(org.name, data);
  }

  render() {
    const { data, createNew, error, loading } = this.props;

    if (error) {
      return <ErrorMessage message={error} retryFn={() => this.load()} />;
    }

    if (loading) {
      return <Loader active />;
    }

    return (
      <div>
        <Header as="h3">{createNew ? 'New project' : `Project ${data.name}`}</Header>
        <ProjectForm
          createNew={createNew}
          originalName={data.name}
          initialValues={rawToForm(data)}
          onSubmit={(data) => this.handleSave(data)}
        />
      </div>
    );
  }
}

const mapStateToProps = ({ project, session }, { params: { projectName } }) => ({
  org: getCurrentOrg(session),
  projectName: projectName,
  createNew: projectName === '_new',
  data: selectors.getData(project),
  error: selectors.getError(project),
  loading: selectors.isLoading(project)
});

const mapDispatchToProps = (dispatch) => ({
  reset: () => {
    dispatch(resetForm('project'));
    dispatch(actions.resetData());
  },

  loadData: (orgName, projectName) => dispatch(actions.loadData([orgName, projectName])),

  saveData: (orgName, data) => {
    const o = formToRaw(data);
    o.orgName = orgName;
    dispatch(actions.saveData(o, [pushHistory('/project/list')]));
  }
});

export default connect(mapStateToProps, mapDispatchToProps)(ProjectPage);

export { reducers, sagas };
