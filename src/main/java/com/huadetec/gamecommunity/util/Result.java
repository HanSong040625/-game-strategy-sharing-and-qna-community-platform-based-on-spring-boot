package com.huadetec.gamecommunity.util;

public class Result<T>{
    private int code ;
    private String message ;
    private T data ;
    private boolean success; // 前端期望的字段
    //T是泛型类型参数，代表任何类型
    private Long total ;//总记录数，用于分页
    public Result(int code, String message , T data)
    {
        this.code = code ;
        this.message = message ;
        this.data = data ;
        this.success = code == 200;
    }
    public Result(int code, String message , T data , Long total)
    {
        this.code = code ;
        this.message = message ;
        this.data = data ;
        this.total = total ;
        this.success = code == 200;
    }
    //this.变量名是new对象的私有变量，给new对象赋值

    public static <T> Result<T> success(T data){
        // static <T> Result<T>静态泛型方法，返回一个包含类型为t的result对象
        //result对象是封装api统一响应格式的实例，包含状态码，消息，数据和总记录数，静态方法无需实例化，实例化就是创建类的具体对象,用的时候直接new
        //响应对象是api返回给前端的结构化数据，静态方法属于类背身，调用时直接通过主类名（文件名）访问，无需创建类的实例
        return new Result(200,"成功",data);
        //<> 是 钻石操作符,用于自动推断类型参数，避免重复声明泛型类型，使用new创建对象时后面跟类名
    }
    public static <T> Result<T> success(T data , Long total){
        return new Result(200, "成功", data , total) ;
    }
    // data是响应的具体数据，成功时加失败不加，message是具体错误信息，失败加成功不加
    public static <T> Result<T> error(int code , String message){//错误响应
        return new Result(code, message , null) ;
    }
    //构造方法用于初始化对象，使用new创建对象时会调用对应的构造方法，调用构造方法是为了给构造方法传参

    public int getCode(){
        return this.code ;
    }

    public void setCode(int code){
        this.code = code ;
    }

    public String getMessage()
    {
        return this.message ;
    }

    public void setMessage(String message)
    {
        this.message = message ;
    }
    public T getData()
    {
        return this.data ;
    }

    public void setData( T data){
        this.data = data ;
    }

    public Long getTotal(){
        return this.total ;
    }

    public void setTotal(Long total){
        this.total = total ;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }

}