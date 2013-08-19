package edu.cmu.lti.oaqa.bio.framework.collection;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.resource.ResourceInitializationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import edu.cmu.lti.oaqa.framework.DataElement;
import edu.cmu.lti.oaqa.framework.DataStore;
import edu.cmu.lti.oaqa.framework.DataStoreImpl;
import edu.cmu.lti.oaqa.framework.collection.IterableCollectionReader;
import edu.cmu.lti.oaqa.framework.collection.impl.DataElementRowMapper;

public final class DBCollectionReader extends IterableCollectionReader {

  JdbcTemplate jdbcTemplate;

  @Override
  protected Iterator<DataElement> getInputSet() throws ResourceInitializationException {
    String url = (String) getConfigParameterValue("url");
    String username = (String) getConfigParameterValue("username");
    String password = (String) getConfigParameterValue("password");

    Integer sequenceStart = (Integer) getConfigParameterValue("sequence-start");
    Integer sequenceEnd = (Integer) getConfigParameterValue("sequence-end");
    String namedSubset = (String) getConfigParameterValue("named-subset");
    Integer[] subset = (Integer[]) getConfigParameterValue("subset");
    try {
      DataStore ds = DataStoreImpl.getInstance(url, username, password);
      this.jdbcTemplate = ds.jdbcTemplate();
      if (subset != null) {
        return getDataSubset(getDataset(), subset);
      } else if (namedSubset != null) {
        return getDataset(getDataset(), namedSubset);
      } else {
        return getDataset(getDataset(), sequenceStart, sequenceEnd);
      }
    } catch (SQLException e) {
      throw new ResourceInitializationException(e);
    }
  }

  protected Iterator<DataElement> getDataset(final String dataset, final Integer startId,
          final Integer endId) throws SQLException {
    List<DataElement> result = jdbcTemplate.query(getSelectQuery(), new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, dataset);
        ps.setInt(2, startId);
        ps.setInt(3, endId);
      }
    }, new DataElementRowMapper());
    return result.iterator();
  }

  protected Iterator<DataElement> getDataset(final String dataset, final String namedSubset)
          throws SQLException {
    List<DataElement> result = jdbcTemplate.query(getSelectNamedSubsetQuery(),
            new PreparedStatementSetter() {
              @Override
              public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, namedSubset);
              }
            }, new DataElementRowMapper());
    return result.iterator();
  }

  protected Iterator<DataElement> getDataSubset(final String dataset, final Integer[] ids)
          throws SQLException {
    List<DataElement> result = jdbcTemplate.query(getSelectSubsetQuery(ids.length),
            new PreparedStatementSetter() {
              @Override
              public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, dataset);
                for (int i = 0; i < ids.length; i++) {
                  ps.setInt(i + 2, ids[i]);
                }
              }
            }, new DataElementRowMapper());
    return result.iterator();
  }

  private String getSelectQuery() {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT id,dataset,sequenceId,question,answerPattern ");
    sb.append(" FROM inputElement WHERE dataset = ? ");
    sb.append(" AND sequenceId >= ? ");
    sb.append(" AND sequenceId <= ? ");
    sb.append(" ORDER by sequenceId ASC");
    return sb.toString();
  }

  private String getSelectSubsetQuery(int len) {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT id,dataset,sequenceId,question,answerPattern ");
    sb.append(" FROM inputElement WHERE dataset = ? ");
    sb.append(" AND sequenceId IN (");
    for (int i = 0; i < len; i++) {
      sb.append("?");
      if (i != len - 1) {
        sb.append(',');
      }
    }
    sb.append(")");
    sb.append(" ORDER by sequenceId ASC");
    return sb.toString();
  }

  private String getSelectNamedSubsetQuery() {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT id, dataset,sequenceId,question,answerPattern ");
    sb.append(" FROM inputElement JOIN named_subset ON (id = inputelementid) ");
    sb.append(" WHERE name = ? ");
    sb.append(" ORDER by sequenceId ASC");
    return sb.toString();
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }
}
