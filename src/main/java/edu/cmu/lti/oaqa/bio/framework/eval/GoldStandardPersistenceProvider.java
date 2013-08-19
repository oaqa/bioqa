package edu.cmu.lti.oaqa.bio.framework.eval;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.oaqa.model.Passage;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import edu.cmu.lti.oaqa.framework.DataStoreImpl;
import edu.cmu.lti.oaqa.framework.eval.gs.AbstractGoldStandardPersistenceProvider;

public class GoldStandardPersistenceProvider extends AbstractGoldStandardPersistenceProvider {

  @Override
  public List<Passage> populateRetrievalGS(final String dataset, final String sequenceId,
          final JCas docGSView) throws SQLException {
    RowMapper<Passage> mapper = new RowMapper<Passage>() {
      @Override
      public Passage mapRow(ResultSet rs, int rowNum) throws SQLException {
        Passage sr = new Passage(docGSView);
        sr.setUri(rs.getString("docId"));
        int begin = rs.getInt("offset");
        int end = begin + rs.getInt("length");
        String aspects = rs.getString("aspects");
        sr.setBegin(begin);
        sr.setEnd(end);
        sr.setRank(rowNum);
        sr.setAspects(aspects);
        return sr;
      }
    };
    String query = getRetrievalGSQuery();
    PreparedStatementSetter pss = new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, dataset);
        ps.setString(2, sequenceId);
      }
    };
    return DataStoreImpl.getInstance().jdbcTemplate().query(query, pss, mapper);
  }

  private String getRetrievalGSQuery() {
    StringBuilder query = new StringBuilder();
    query.append("SELECT dataset,sequenceId,docId,offset,length,aspects FROM retrieval_gs");
    query.append(" WHERE dataset = ? AND sequenceId = ? ");
    return query.toString();
  }

}
