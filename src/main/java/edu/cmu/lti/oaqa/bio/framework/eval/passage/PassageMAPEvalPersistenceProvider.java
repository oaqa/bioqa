package edu.cmu.lti.oaqa.bio.framework.eval.passage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import edu.cmu.lti.oaqa.ecd.phase.Trace;
import edu.cmu.lti.oaqa.framework.DataStoreImpl;
import edu.cmu.lti.oaqa.framework.eval.ExperimentKey;
import edu.cmu.lti.oaqa.framework.eval.Key;
import edu.cmu.lti.oaqa.framework.eval.passage.PassageMAPCounts;
import edu.cmu.lti.oaqa.framework.eval.passage.PassageMAPEvaluationData;
import edu.cmu.lti.oaqa.framework.persistence.AbstractPassageMAPEvalPersistenceProvider;

public class PassageMAPEvalPersistenceProvider extends AbstractPassageMAPEvalPersistenceProvider {

  @Override
  public void deletePassageAggrEval(final Key key, final String sequenceId) {
    final String name = getClass().getSimpleName();
    String insert = getDeletePassageAggrEval();
    DataStoreImpl.getInstance().jdbcTemplate().update(insert, new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, key.getExperiment());
        ps.setString(2, key.getTrace().getTraceHash());
        ps.setString(3, name);
        ps.setString(4, sequenceId);
      }
    });
  }

  @Override
  public void insertPartialCounts(final Key key, final String sequenceId,
          final PassageMAPCounts counts) throws SQLException {
    final String eName = getClass().getSimpleName();
    String insert = getInsertPassageAggregates();
    final Trace trace = key.getTrace();
    DataStoreImpl.getInstance().jdbcTemplate().update(insert, new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, key.getExperiment());
        ps.setString(2, trace.getTrace());
        ps.setString(3, eName);
        ps.setFloat(4, counts.getDocavep());
        ps.setFloat(5, counts.getPsgavep());
        ps.setFloat(6, counts.getPsg2avep());
        ps.setFloat(7, counts.getAspavep());
        ps.setFloat(8, counts.getCount());
        ps.setString(9, sequenceId);
        ps.setInt(10, key.getStage());
        ps.setString(11, trace.getTraceHash());
      }
    });
  }

  @Override
  public Multimap<Key, PassageMAPCounts> retrievePartialCounts(final ExperimentKey experiment) {
    String select = getSelectPassageAggregates();
    final Multimap<Key, PassageMAPCounts> counts = LinkedHashMultimap.create();
    RowCallbackHandler handler = new RowCallbackHandler() {
      public void processRow(ResultSet rs) throws SQLException {
        Key key = new Key(rs.getString("experimentId"), new Trace(rs.getString("traceId")),
                rs.getInt("stage"));
        PassageMAPCounts cnt = new PassageMAPCounts(rs.getFloat("docavep"), rs.getFloat("psgavep"),
                rs.getFloat("psg2avep"), rs.getFloat("aspavep"), rs.getInt("count"));
        counts.put(key, cnt);
      }
    };
    DataStoreImpl.getInstance().jdbcTemplate().query(select, new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, experiment.getExperiment());
        ps.setInt(2, experiment.getStage());
      }
    }, handler);
    return counts;
  }

  @Override
  public void deletePassageMeasureEval(final ExperimentKey experiment) {
    String insert = getDeletePassageMeasureEval();
    DataStoreImpl.getInstance().jdbcTemplate().update(insert, new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, experiment.getExperiment());
        ps.setInt(2, experiment.getStage());
      }
    });
  }

  @Override
  public void insertMAPMeasureEval(final Key key, final String eName,
          final PassageMAPEvaluationData eval) throws SQLException {
    String insert = getInsertMAPMeasureEval();
    final Trace trace = key.getTrace();
    DataStoreImpl.getInstance().jdbcTemplate().update(insert, new PreparedStatementSetter() {
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, key.getExperiment());
        ps.setString(2, trace.getTrace());
        ps.setString(3, eName);
        ps.setFloat(4, eval.getDocMap());
        ps.setFloat(5, eval.getPsgMap());
        ps.setFloat(6, eval.getPsg2Map());
        ps.setFloat(7, eval.getAspMap());
        ps.setFloat(8, eval.getCount());
        ps.setInt(9, key.getStage());
        ps.setString(10, trace.getTraceHash());
      }
    });
  }

  private String getInsertPassageAggregates() {
    StringBuilder query = new StringBuilder();
    query.append("INSERT INTO map_aggregates_str");
    query.append(" (experimentId, traceId, aggregator, ");
    query.append("docavep, psgavep, psg2avep, aspavep, count, sequenceId, stage,traceHash) ");
    query.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?)");
    return query.toString();
  }

  private String getDeletePassageAggrEval() {
    StringBuilder query = new StringBuilder();
    query.append("DELETE FROM map_aggregates_str WHERE ");
    query.append(" experimentId = ? AND traceHash = ? AND aggregator = ? AND sequenceId = ?");
    return query.toString();
  }

  private String getSelectPassageAggregates() {
    StringBuilder query = new StringBuilder();
    query.append("SELECT experimentId, traceId, ");
    query.append(" docavep, psgavep, psg2avep, aspavep, count, stage ");
    query.append(" FROM map_aggregates_str WHERE experimentId = ? AND stage = ?");
    return query.toString();
  }

  private String getDeletePassageMeasureEval() {
    StringBuilder query = new StringBuilder();
    query.append("DELETE FROM map_eval WHERE ");
    query.append(" experimentId = ? AND stage = ?");
    return query.toString();
  }

  private String getInsertMAPMeasureEval() {
    StringBuilder query = new StringBuilder();
    query.append("INSERT INTO map_eval");
    query.append(" (experimentId, traceId, evaluator, ");
    query.append(" docmap, psgmap, psg2map, aspmap,count,stage,traceHash) ");
    query.append(" VALUES (?,?,?,?,?,?,?,?,?,?)");
    return query.toString();
  }

}
