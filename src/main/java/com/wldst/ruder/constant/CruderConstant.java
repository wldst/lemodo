package com.wldst.ruder.constant;

//import com.wldst.ruder.util.StaticTemplate;

/**
 * 营销label MKLabel
 * 
 * @author liuqiang
 * @date 2019年9月18日 下午5:10:01
 * @version V1.0
 */
public class CruderConstant extends Msg{
//    @Autowired
//    protected StaticTemplate st;
    
    public final static String COLUMNS = "columns";
    public final static String CONTENT = "content";
    public final static String SEARCH_VAL = "searchVal";
    public final static String KEY_WORD = "KeyWord";
    public final static String TITLE = "title";
    
    public final static String STATUS = "status";
    public final static String DATA = "data";
    
     
    public final static String HEADER = "header";
    public final static String COLUMN_VALIDATOR = "validator";
    public final static String FIELD = "field";
    public final static String FIELD_DEFAULT_VALUE = "defaultValue";
    public final static String LABEL_FIELD = "Field";
    
    public final static String COLUMN_FORMAT="FmtCol";
    
    public final static String FIELD_TYPE = "FieldType";
    public final static String SHOW_TYPE = "showType";
    public final static String URL = "url";
    
    //layui相关信息
    public final static String LAY_USE ="layUse";
    public final static String HAS_TEXT_EDITOR ="hasTextEditor";
    public final static String TEXT_EDITOR ="hasTextEditor";
    public final static String FORM_VERIFY_JS ="formVerifyJs";
    public final static String EDIT_INDEX ="editIndex";
    public final static String REGEX_NODE_LABEL="^[a-zA-Z][a-zA-Z0-9_]*$";
    /**
     * 表单校验信息
     * @param formVerifyJs
     * @param code
     */
    public void formVerify(StringBuilder formVerifyJs, String code) {
	if(formVerifyJs.length()>0) {
	    formVerifyJs.append("\n,");
	}
	formVerifyJs.append(code+": function(value) {\n" );
	formVerifyJs.append("        return layedit.sync("+EDIT_INDEX+"['"+code+"']);\n}");
    }
    
    public final static String HAS_DATE_FIELD ="hasDateField";
    public final static String HAS_FILE ="hasFile";
    

    public static final String TYPE = "type";
    public static final String FILE_TYPE_XLS = ".xls";
    public static final String FILE_TYPE_XLSX = ".xlsx";
    public final static String PREFIX = "cruder";

    public final static String NAVI_LABEL = "naviLabel";
    public final static String TABLE_LABEL = "tableLabel";
    public final static String NODE_PROP = "properties";

    public final static String NODE_COLUMN = COLUMNS;

    public final static String ID = "id";
    public final static String ID_BIG = "ID";
    public final static String INSTANCE_ID ="instanceId";
    public final static String POLABEL_ID ="poLabelId";
    public final static String CODE = "code";
    public final static String NAME = "name";
    public final static String LABEL = "label";
    public final static String VALUE = "value";

    public final static String NODE_ID = ID;
    public final static String NODE_CODE = CODE;
    public final static String NODE_NAME = NAME;
    public final static String NODE_LABEL = LABEL;
    
    public final static String CREATETIME = "createTime";    
    public final static String UPDATETIME = "updateTime";    
    public final static String UPDATOR = "updator";
    public final static String CREATOR = "creator";
    
    public final static String NEED_SIMPLE = "creator,createTime,updator,updateTime,time";
    
    
    
    public final static String KEY = "key";
    public final static String IS_TREE_ROOT="isRoot";
    public final static String PARENT_ID="parentId";
    public final static String REL_START_ID="relStartId";
    public final static String REL_START_LABEL="relStartLabel";
    
    public final static String DOMAIN = "Domain";
    public final static String DOMAIN_KEY = "label";
    public final static String DOMAIN_COLUMN = "label,name,tableName,columns,header,description,primaryKey";

    
    public final static String CYPHER_ACTION = "Action";
    public final static String DATA_IMPORT = "DataImport";
    public final static String CYPHER = "cypher";
    public final static String META_DATA = "MetaData";
    //无码
    public final static String WUMA = "WuMaData";
    public final static String WUMA_NAME = "WuMa";
    public final static String WUMA_CODE = "wmCode";
    public final static String WUMA_COLUMN = "wmc";
    public final static String RESOURCE = "resource";
    public final static String MARK_LABEL = "MARK_LABEL";
    public final static String VALUE_FIELD = "valueField";
    
    public final static String PO_KEY = "label";
    public final static String PO = "Po";
    public final static String PO_COLUMN = "id,label,name,tableName,columns,header,description,primaryKey";

    public final static String MODULE = "module";

    public final static String SELECT_COLUMN = "id,code,name";
    public final static String INTERFACE_COLUMN = "id,name";

    public final static String REALTION = "relation";
    public final static String CRUD_KEY = "primaryKey";
   
    public final static String VIEW = "View";
    public final static String VIEW_KEY = "ViewKey";
    public final static String VIEW_COLUMN = "label,name,cql,columns,header,description";

    public final static String VO = "Vo";
    public final static String VO_COLUMN = "voColumns";

    // Domain label
    public final static String QUALITY = "";
    public final static String CHECK_OBJECT = "CheckObject";
    // relation label
    public final static String RULE_DEFINE = "ruleDefine";
    public final static String HAS_RULE = "hasRule";
    // 使用中的规则
    public final static String USING_RULE = "usingRule";

    public final static String RELATION_ENDNODE_PROP = "endNodeProperties";
    public final static String RELATION_ENDNODE_LABEL = "endLabels";
    public final static String RELATION_STARTNODE_PROP = "startNodeProperties";
    public final static String RELATION_STARTNODE_LABEL = "startLabels";
    public final static String RELATION_PROP = "relProps";
    public final static String RELATION_TYPE = "relType";
    public final static String RELATION_START_ID = "startId";
    public final static String RELATION_END_ID = "endId";

    public final static String RELATION_DEFINE = "RelationDefine";
    public final static String START_LABEL = "startLabel";
    public final static String RELATION_LABEL = "reLabel";
    public final static String END_LABEL = "endLabel";
    public final static String RELATION_PARTS = "parts";

    public final static String REL_TYPE_CHILDREN = "children";
    public final static String REL_TYPE_CHILDRENS = "childrens";
    public final static String REL_END = "relEnds";
    
    public final static String REL_TYPE_PARENT = "parent";
    public final static String COLUMN_PARENT = "parentId";

    public final static String PO_PROP_SPLITER = "-_-";
    public final static String TABLE_TEMPLATE_ID = "templateId";
    public final static String TABLE_TEMPLATE_CONTENT = "templateContent";

    public final static String COPY_RELATION = "copiable";

    public static final String FEN_HAO = ":";
    
    
    //操作值
    public static final String ON = "on";
//    public static final String VERSION = "version";
    protected static final String VERSION_DATA = "dataVersion";
    
}
