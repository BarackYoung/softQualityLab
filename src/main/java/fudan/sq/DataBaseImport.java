package fudan.sq;

import java.io.*;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class DataBaseImport {
    //jdbc驱动
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/?&useSSL=false&serverTimezone=UTC";    //注意此处需要添加时区等信息，否则会连接失败


    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        System.out.println("请输入本地mysql数据库的user名称:");
        Scanner scName = new Scanner(System.in);
        String user = scName.next();
        System.out.println("请输入本地mysql数据库的password名称");
        Scanner scPassword = new Scanner(System.in);         //控制台获取字符串
        String password = scPassword.next();   //获取控制台输入string
        System.out.println("... 数据库创建准备中，请确保您已配置mysql驱动 ...");
        System.out.println("...贷款理财产品数据库将被在本地被创建为 Stock...");



        Connection conn = null;
        Statement stmt = null;

        //创建stock数据库
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, user, password);
            //判断数据库连接是否成功
            if (!conn.isClosed()) {
                System.out.println("数据库连接成功");
            }

            stmt = conn.createStatement();
            //数据库创建语句
            String sql = "CREATE DATABASE stock";
            stmt.executeUpdate(sql);
            System.out.println("... Stock数据库创建成功 ...");
            stmt.close();
            conn.close();
        }catch (ClassNotFoundException e) {
            System.out.println("数据库驱动没有安装");
            System.out.println("安装步骤：STEP1：下载mysql最新jar包（本项目lib文件夹下)");
            System.out.println("          STEP2：File -> Project Structure -> module -> add JARs or directories -> mysql Jar");
            System.out.println("          STEP3：APPLY ! ");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库连接失败");
        }

        //连接上一步创建的数据库stock
        try {
            Class.forName(JDBC_DRIVER);
            String url = "jdbc:mysql://localhost:3306/" + "stock" + "?&useSSL=false&serverTimezone=UTC";
            conn = DriverManager.getConnection(url, user, password);
            //判断数据库连接是否成功
            if (!conn.isClosed()) {
                System.out.println("数据库Stock连接成功");
            }
        }catch (ClassNotFoundException e) {
            System.out.println("JDBC驱动失效");
        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库连接失败");
        }

        //创建Stock表
        try {
            stmt = conn.createStatement();
            //数据库创建语句
            String sql = "CREATE TABLE stock("
                    + " productId int(11), "           /*产品ID*/
                    + " productName VARCHAR(45), "     /*产品名称*/
                    + " productPrice double, "             /*当天股票单价*/   /* 注意单位为元 */
                    + " date date, "                 /*日期*/
                    + " PRIMARY KEY (productId,date) " /*主键为产品ID + 日期，唯一定义一个产品的变化*/
                    + ")";
            stmt.executeUpdate(sql);
            System.out.println("... Stock数据库中stock表创建成功 ...");
        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println("stockTable创建失败");
        }

        //创建Fund表
        try {
            stmt = conn.createStatement();
            //数据库创建语句
            String sql = "CREATE TABLE fund("
                    + " productId int(11), "            /*产品ID*/
                    + " productName VARCHAR(45), "      /*产品名称*/
                    + " rate double, "                 /*当天利率*/  /* 注意单位为 %  */
                    + " date date, "                    /*日期*/
                    + " PRIMARY KEY (productId,date) " /*主键为产品ID + 日期，唯一定义一个产品的变化*/
                    + ")";
            stmt.executeUpdate(sql);
            System.out.println("... Stock数据库中fund表创建成功 ...");
        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println("fundkTable创建失败");
        }

        //创建Regular表
        try {
            stmt = conn.createStatement();
            //数据库创建语句
            String sql = "CREATE TABLE regular("
                    + " productId int(11), "           /*产品ID*/
                    + " productName VARCHAR(45), "     /*产品名称*/
                    + " productPrice int(11),"          /*单价（如 一万）*/ /* 注意单位为元 */
                    + " period int(11), "              /*时长（如 三年）*/  /* 注意单位为年 */
                    + " rate double, "                /*利率（如 0.05%）*/  /* 注意单位为 %  */
                    /* 死期不需要日期 */
                    + " PRIMARY KEY (productId) "      /*主键为产品ID ，唯一定义一个产品的变化*/
                    + ")";
            stmt.executeUpdate(sql);
            System.out.println("... Stock数据库中regular表创建成功 ...");
        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println("regularTable创建失败");
        }


        //创建property表
        try {
            stmt = conn.createStatement();
            //数据库创建语句
            String sql = "CREATE TABLE property("
                    + " recordID int(11), "      /*购买记录ID，由后端生成*/
                    + " customerID VARCHAR(225), "    /*客户ID，前端传入*/
                    + " productID int(11), "     /*产品ID,与产品表中相对应*/
                    + " amount int(11), "
                    /*对于基金，为购买的金额，盈亏计算方式为 金额*利率  */
                    /*对于股票，为购买的股数，.............  股数*每股的价格*/
                    /*对于定期，为购买的倍数（三万元三年利率为0.05，一次购买九万就是三倍），只有赢利*/
                    + " purchaseDay date ,"      /*购入时间*/
                    + " PRIMARY KEY (recordID) " /*主键为购买记录ID*/
                    + ")";
            /*主键为购买记录，因为同一个客户可以在不同时间多次购买同一件产品，将会生成不同的购买记录*/
            /*对于收益亏损情形的计算为，根据当前时间和购入时间之间的产品price进行计算*/
            stmt.executeUpdate(sql);
            System.out.println("... Stock数据库中property表创建成功 ...");


        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println("propertyTable创建失败");
        }

        //向stock表中插入数据
        stmt = conn.createStatement();
        String sql;

        //产品1(股票)
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 10000, '2021-03-30')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 9100, '2021-03-31')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 7700, '2021-04-01')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 8800, '2021-04-02')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 8000, '2021-04-03')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 7000, '2021-04-04')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 7500, '2021-04-05')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 7250, '2021-04-06')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 8100, '2021-04-07')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 8700, '2021-04-08')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 8800, '2021-04-09')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 8400, '2021-04-10')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 8600, '2021-04-11')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 8650, '2021-04-12')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 8900, '2021-04-13')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 9500, '2021-04-14')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000001, '001股票', 9250, '2021-04-15')";
        stmt.executeUpdate(sql);


        //产品2(基金)
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.05, '2021-03-30')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.06, '2021-03-31')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.08, '2021-04-01')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.03, '2021-04-02')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.07, '2021-04-03')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.02, '2021-04-04')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', -0.01, '2021-04-05')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', -0.03, '2021-04-06')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', -0.07, '2021-04-07')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', -0.05, '2021-04-08')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', -0.01, '2021-04-09')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.01, '2021-04-10')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.04, '2021-04-11')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.05, '2021-04-12')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.01, '2021-04-13')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.06, '2021-04-14')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000002, '1314基金', 0.11, '2021-04-15')";
        stmt.executeUpdate(sql);


        //产品4(基金)
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', 0.11, '2021-03-30')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', 0.13, '2021-3-31')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', 0.09, '2021-04-01')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', 0.05, '2021-04-02')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', 0.07, '2021-04-03')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', 0.01, '2021-04-04')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', 0.03, '2021-04-05')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', -0.02, '2021-04-06')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', -0.03, '2021-04-07')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', -0.07, '2021-04-08')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', -0.09, '2021-04-09')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', -0.13, '2021-04-10')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', -0.08, '2021-04-11')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', -0.02, '2021-04-12')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', 0.01, '2021-04-13')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', 0.03, '2021-04-14')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO fund VALUES (00000004, '5555基金', 0.04, '2021-04-15')";
        stmt.executeUpdate(sql);


        //产品5(股票)
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 10000, '2021-03-30')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 20000, '2021-03-31')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 30000, '2021-04-01')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 40000, '2021-04-02')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 50000, '2021-04-03')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 60000, '2021-04-04')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 70000, '2021-04-05')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 80000, '2021-04-06')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 90000, '2021-04-07')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 100000, '2021-04-08')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 110000, '2021-04-09')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 120000, '2021-04-10')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 130000, '2021-04-11')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 140000, '2021-04-12')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 150000, '2021-04-13')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 160000, '2021-04-14')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000005, 'zc&cm&hyq-发发发', 170000, '2021-04-15')";
        stmt.executeUpdate(sql);

        //产品3(定期)
        sql = "INSERT INTO regular VALUES (00000003, '定期1号', 30000, 3 ,0.06)";
        stmt.executeUpdate(sql);
        //产品6(定期)
        sql = "INSERT INTO regular VALUES (00000006, '定期2号', 60000, 3 ,0.09)";
        stmt.executeUpdate(sql);
        //产品7(定期)
        sql = "INSERT INTO regular VALUES (00000007, '定期3号', 10000, 1 ,0.01)";
        stmt.executeUpdate(sql);
        //产品8(定期)
        sql = "INSERT INTO regular VALUES (00000008, '定期4号', 30000, 1 ,0.03)";
        stmt.executeUpdate(sql);


        //产品9(股票)
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 10000, '2021-03-30')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 20000, '2021-03-31')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 30000, '2021-04-01')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 40000, '2021-04-02')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 50000, '2021-04-03')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 60000, '2021-04-04')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 70000, '2021-04-05')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 80000, '2021-04-06')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 90000, '2021-04-07')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 100000, '2021-04-08')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 110000, '2021-04-09')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 120000, '2021-04-10')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 130000, '2021-04-11')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 140000, '2021-04-12')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 150000, '2021-04-13')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 160000, '2021-04-14')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000009, '全都拿A', 170000, '2021-04-15')";
        stmt.executeUpdate(sql);

        //产品10(股票)
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 100, '2021-03-30')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 111, '2021-03-31')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 107, '2021-04-01')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 123, '2021-04-02')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 151, '2021-04-03')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 91, '2021-04-04')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 93, '2021-04-05')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 87, '2021-04-06')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 89, '2021-04-07')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 97, '2021-04-08')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 103, '2021-04-09')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 107, '2021-04-10')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 109, '2021-04-11')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 102, '2021-04-12')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 98, '2021-04-13')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 97, '2021-04-14')";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO stock VALUES (00000010, '股票111号', 99, '2021-04-15')";
        stmt.executeUpdate(sql);

        //产品12（股票）
        createProduct("12","快乐柠檬",conn,1000,"stock");

        //产品13（股票）
        createProduct("13","五十岚",conn,1200,"stock");

        //产品14（股票）
        createProduct("14","一点点",conn,1500,"stock");

        //产品15（股票）
        createProduct("15","CoCo",conn,500,"stock");

        //产品16（基金）
        createProduct("16","DQ",conn,1200,"fund");

        //产品17（基金）
        createProduct("17","八喜",conn,1800,"fund");

        //产品18（基金）
        createProduct("18","可爱多",conn,900,"fund");

        //产品18（基金）
        createProduct("19","梦龙",conn,400,"fund");




        //关闭连接
        stmt.close();
        conn.close();

    }

    public static void createProduct(String id,String name,Connection conn,int myprice,String tableName) throws SQLException {

        int price=myprice;

        Statement statement = conn.createStatement();
        Random r = new Random(1);


        int ran1 = r.nextInt(20);

        price=price*(100+ran1-10)/100;
        String str="INSERT INTO "+tableName+" VALUES (000000"+id+", \'"+name+"\', "+price+", '2021-03-30')";

        System.out.println(str);
        statement.executeUpdate(str);

        ran1 = r.nextInt(20);

        price=price*(100+ran1-10)/100;
        str="INSERT INTO "+ tableName + " VALUES (000000"+id+",\' "+name+"\', "+price+", '2021-03-31')";
        statement.executeUpdate(str);

        for(int i=1;i<10;i++){

            ran1 = r.nextInt(20);

            price=price*(100+ran1-10)/100;
            str="INSERT INTO "+ tableName +" VALUES (000000"+id+", \'"+name+"\', "+price+", '2021-04-0"+i+"')";
            statement.executeUpdate(str);
        }

        for(int i=10;i<=16;i++){

            ran1 = r.nextInt(20);

            price=price*(100+ran1-10)/100;
            str="INSERT INTO "+ tableName +" VALUES (000000"+id+", \'"+name+"\', "+price+", '2021-04-0"+i+"')";
            statement.executeUpdate(str);
        }

    }
}



