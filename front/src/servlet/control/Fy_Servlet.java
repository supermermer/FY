package servlet.control;

import bean.data.Fy_Bean;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;

/**
 * @author Jagger
 * @ClassName Fy_Servlet.java
 * @Description 用于读取和按条件搜索数据库中fy_inf的信息，并存入Fy_Bean
 * @createTime 2022/1/12
 */
public class Fy_Servlet extends HttpServlet {
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        request.setCharacterEncoding("gb2312");
//        String dataBase= request.getParameter("dataBase");
//        String tableName= request.getParameter("tableName");
//        String user= request.getParameter("user");
//        String password= request.getParameter("password");
//        boolean boo =( dataBase==null||dataBase.length()==0);
//        boo = boo||( tableName==null||tableName.length()==0);
//        boo = boo||( user==null||user.length()==0);
//        if(boo) {
//            fail(request,response,"查询失败");
//        }
        request.setCharacterEncoding("gb2312");//防止乱码！！！！
        String dataBase = "hrm_db";
        String tableName = "fy_inf";
        String user = "root";
        String password = "root";
        String[][] searchSheet_upload = {{"黑龙江省", "吉林省", "辽宁省"}, {"2006(第一批)", "2008(第二批)", "2011(第三批)", "2014(第四批)", "2021(第五批)"}, {"民间文学", "传统音乐", "传统舞蹈", "传统戏剧", "曲艺", "传统体育、游艺与杂技", "传统美术", "传统技艺", "传统医药", "民俗"}, {"新增项目", "扩展项目"}};
        String[] sqlColumn = {"申报地区或单位", "公布时间", "类别", "类型"};

        //动态拼接sql语句的条件
//        System.out.println(request.getParameter("select-province"));
        int select_province = Integer.parseInt(request.getParameter("select-province").trim());
        int select_publishTime = Integer.parseInt(request.getParameter("select-publishTime").trim());
        int select_native = Integer.parseInt(request.getParameter("select-native").trim());
        int select_type = Integer.parseInt(request.getParameter("select-type").trim());
        String select_name = request.getParameter("select-name").trim();
        String currentSql = "SELECT * FROM " + tableName;

        if (select_name.equals("")) {
            int[] sqlProperty = {select_province, select_publishTime, select_native, select_type};
            int j = 0;
            for (int i = 0; i < sqlProperty.length; i++) {
                if (sqlProperty[i] != 0) {
                    if (j == 0) {
                        currentSql += " where ";
                    } else currentSql += " and ";
                    j++;
                    if (i == 0) {
                        currentSql += sqlColumn[i] + " like '" + searchSheet_upload[i][sqlProperty[i] - 1] + "%'";//模糊搜索 只要是这个省的就都要
                    } else {
                        currentSql += sqlColumn[i] + " = '" + searchSheet_upload[i][sqlProperty[i] - 1] + "'";
                    }
                }
            }
        } else {
            currentSql += " where name = '" + select_name + "'";
        }


        HttpSession session = request.getSession(true);
        Connection con = null;
        Fy_Bean recordBean = null;
        try {
            recordBean = (Fy_Bean) session.getAttribute("recordBean");
            if (recordBean == null) {
                recordBean = new Fy_Bean();  //创建Javabean对象
                session.setAttribute("recordBean", recordBean);
            }
        } catch (Exception exp) {
            recordBean = new Fy_Bean();
            session.setAttribute("recordBean", recordBean);
        }
        String uri = "jdbc:mysql://127.0.0.1/" + dataBase;
        try {
            con = DriverManager.getConnection(uri, user, password);
            Statement sql = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = sql.executeQuery(currentSql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount(); //得到结果集的列数
            String[] columnName = new String[columnCount];
            for (int i = 0; i < columnName.length; i++) {
                columnName[i] = metaData.getColumnName(i + 1); //得到列名
            }
            recordBean.setColumnName(columnName);   //更新Javabean数据模型
            rs.last();
            int rowNumber = rs.getRow();  //得到记录数
            String[][] tableRecord = recordBean.getTableRecord();
            tableRecord = new String[rowNumber][columnCount];
            rs.beforeFirst();
            int i = 0;
            while (rs.next()) {
                for (int k = 0; k < columnCount; k++)
                    tableRecord[i][k] = rs.getString(k + 1);
                i++;
            }
            recordBean.setTableRecord(tableRecord); //更新Javabean数据模型

            con.close();
            response.sendRedirect("searchSheet.jsp");  //重定向
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

}
