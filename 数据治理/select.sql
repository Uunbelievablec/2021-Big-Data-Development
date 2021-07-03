// 按月查看总体销售额
select sum(sale * price), month from (select * from 商品,销售 where 商品.id = 销售.product_id ) as a group by month; 

// 按月查看新增用户人数
select sum(id) as "sum" , "month" from 用户 group by "month" ; 

// 按月查看城市销售额
select city, 销售."month" as m , sum("sum" * price) from 
(select * from 用户, 销售, 商品 where 用户.id = 销售.user_id and 商品.id = 销售.product_id ) as a
group by 销售."month" , city ;

// 按月查看城市、商品类别的销售额
with a as (
select city, 销售."month" m , sum("sum" * price) s from 
(select * from 用户, 销售, 商品 where 用户.id = 销售.user_id and 商品.id = 销售.product_id ) as a1
group by 销售."month" , city)
select a.city,a.s city_sum, b.category, b.s cate_sum from a,
(select category, "month" m, sum(sale * price) s from 
(select * from 销售,商品 where 销售.product_id = 商品.id ) as b1 group by "month" , category) b where a.m = b.m;

// 按月查看性别、商品类别的销售额
with a as (
select gender, 销售."month" m , sum("sum" * price) s from 
(select * from 用户, 销售, 商品 where 用户.id = 销售.user_id and 商品.id = 销售.product_id ) as a1
group by 销售."month" , gender)
select a.gender,a.s gender_sum, b.category, b.s cate_sum from a,
(select category, "month" m, sum(sale * price) s from 
(select * from 销售,商品 where 销售.product_id = 商品.id ) as b1 group by "month" , category) b where a.m = b.m;
