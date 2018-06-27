package cn.luischen.controller.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 查询结果对象
 *
 * @author tangjiawei
 * @version 0.1
 */
public class QueryResult<T> implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 3220476770000595544L;
	/**
     * 当前页结果集
     */
    private List<T> resultList;
    /**
     * 记录总数(不计算过滤条件)
     */
    private Long totalCount = 0l;
    /**
     * 记录总数(符合过滤条件的数量)
     */
    private Long totalRecord = 0l;

    /**
     * 每页记录数
     */
    private Integer pageSize;

    /**
     * 当前页号
     */
    private Integer currPageNo;

    /**
     * 总页数
     */
    private Long pageCount;

    public List<T> getResultList() {
        return resultList;
    }

    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }

    public Long getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(Long totalRecord) {
        this.totalRecord = totalRecord;
        calculatePageCount();
    }

    /**
     * 计算查询的起始记录号
     *
     * @param pageSize 每页记录数
     * @param currpageNo 当前页号
     * @return 当前页起始记录号
     */
    public static Integer calculateFirstResult(Integer pageSize, Integer currpageNo) {
        if (pageSize != null && currpageNo != null) {
            Integer pageNo = currpageNo - 1;
            return pageSize * pageNo;
        }
        return null;
    }

    /**
     * 计算查询的起始记录号
     *
     * @param condition 分页查询条件对象
     */
    /*public static Integer calculateFirstResult(IQueryWithPage condition) {
     return calculateFirstResult(condition.getPageSize(), condition.getPageNo());
     }*/
    /**
     * 计算当前页号
     *
     * @param firstResultNo
     */
    public void calculateCurrPageNo(Integer firstResultNo) {
        if (currPageNo != null) {
            return;
        }
        currPageNo = firstResultNo / pageSize;
        //if (firstResultNo % pageSize > 0)
        currPageNo++;
    }

    /**
     * 计算总页数
     */
    private void calculatePageCount() {
        //有些情况不需要计算页数，没有传递pageNo, pageSize
        if (null == pageSize) {
            return;
        }
        pageCount = totalRecord / pageSize;
        if (totalRecord % pageSize != 0) {
            pageCount++;
        }
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getPageCount() {
        return pageCount;
    }

    public void setPageCount(Long pageCount) {
        this.pageCount = pageCount;
    }

    public Integer getCurrPageNo() {
        return currPageNo;
    }

    public void setCurrPageNo(Integer currPageNo) {
        this.currPageNo = currPageNo;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public QueryResult() {
    }
    
    public QueryResult(T o) {
    	this.setResultList(new ArrayList<T>(Arrays.asList(o)));
    	this.totalRecord = 1L;
    	this.currPageNo = 1;
    	this.pageCount =1L;
    	this.totalCount=1L;
    }

    public QueryResult(List<T> resultList, long totalRecord, int currPageNo, int pageSize) {
        this.resultList = resultList == null ? new ArrayList<T>() : resultList;
        this.totalRecord = totalRecord;
        this.currPageNo = currPageNo;
        this.pageSize = pageSize;
        
        if (this.totalRecord <= 0) {
            this.totalRecord = 0l;
            this.pageCount = 1l;
        }else if(this.pageSize <= 0){
        	this.pageCount = 1l;
        }else {
        	this.pageCount = (totalRecord + pageSize - 1) / pageSize;
        }
        
        if(this.resultList==null){
            this.resultList = new ArrayList();
        }
        
        /*
        int countPage = (new Long(this.pageCount)).intValue();
        if (this.currPageNo <= 1) {
            this.currPageNo = 1;
        } else if (this.currPageNo >= countPage) {
            this.currPageNo = countPage;
        }
        */
        
    }

    @Override
    public String toString() {
        return String.format("QueryResult [resultList=%s, totalRecord=%s, pageSize=%s, currPageNo=%s, pageCount=%s]", resultList, totalRecord,
                pageSize, currPageNo, pageCount);
    }
    
    public static QueryResult queryJdbcTemplateData(JdbcTemplate jdbcTemplate,String sql, int pageNo, int pageSize) {
		sql = StringUtils.trim(sql);
		String pageSql="";
		long totalRecord=0;
		if (pageNo > 0 && pageSize > 0) {
			String countSql = "select count(*) from (" + sql + ") as totalNum";
			totalRecord = jdbcTemplate.queryForList(countSql.toString()).size();
			pageSql="select * from ("+sql+") rs where rs.rowid>"+(pageNo-1)*pageSize+" and rs.rowid<="+pageNo*pageSize;
		}else{
			pageSql=sql;
		}
		List<Map<String, Object>> list = jdbcTemplate.queryForList(pageSql.toString());
		return new QueryResult(list, totalRecord, pageNo, pageSize);
	}
    
}
