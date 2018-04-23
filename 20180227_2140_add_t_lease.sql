create table t_lease (
  id number(38) primary key,
  name varchar(200) not null,
  cookie varchar(80) not null,
  holder varchar(80) not null,
  expiry_at timestamp null
)
/

create unique index idx_lease_name on t_lease ( lower(name) )
/

create sequence s_lease start with 10000
/
