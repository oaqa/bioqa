package edu.cmu.lti.oaqa.bio.framework.collection;

import java.sql.SQLException;

import javax.jms.MapMessage;

import org.apache.uima.resource.ResourceInitializationException;
import org.springframework.jdbc.core.JdbcTemplate;

import edu.cmu.lti.oaqa.framework.DataElement;
import edu.cmu.lti.oaqa.framework.DataStoreImpl;
import edu.cmu.lti.oaqa.framework.collection.AbstractCollectionReaderConsumer;
import edu.cmu.lti.oaqa.framework.collection.impl.DataElementRowMapper;

public final class DBCollectionReaderConsumer extends AbstractCollectionReaderConsumer {

  private JdbcTemplate jdbcTemplate;

  @Override
  public void initialize() throws ResourceInitializationException {
    super.initialize();
    try {
      initdb();
    } catch (SQLException e) {
      throw new ResourceInitializationException(e);
    }
  }

  private void initdb() throws SQLException {
    String url = (String) getConfigParameterValue("url");
    String username = (String) getConfigParameterValue("username");
    String password = (String) getConfigParameterValue("password");
    this.jdbcTemplate = DataStoreImpl.getInstance(url, username, password).jdbcTemplate();
  }

  protected DataElement getDataElement(MapMessage map) throws Exception {
    String dataset = map.getString("dataset");
    String seqId = map.getString("sequenceId");
    DataElement result = jdbcTemplate.queryForObject(getSelectQueryForId(),
            new DataElementRowMapper(), dataset, seqId);
    return result;
  }

  private String getSelectQueryForId() {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT id,dataset,sequenceId,question,answerPattern ");
    sb.append(" FROM inputElement WHERE dataset = ? ");
    sb.append(" AND sequenceId = ? ");
    sb.append(" ORDER by sequenceId ASC");
    return sb.toString();
  }
}
