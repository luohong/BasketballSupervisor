BEGIN TRANSACTION;

-- 球员动作行为表（比如投篮）
CREATE TABLE tb_action (
	_id INTEGER  PRIMARY KEY AUTOINCREMENT ,
	next_action_id INTEGER ,--下一个动作的id（创新数据，还有下一个选项的）
	name TEXT ,-- 动作名称，如投篮
	score INTEGER ,-- 得分
	cancelable INTEGER  -- 对话框是否可以取消
 );
 
 -- 比赛表
CREATE TABLE tb_game (
	_id LONG  PRIMARY KEY ,
	p_id LONG ,-- 平台id
	name TEXT ,-- 比赛名称
	remark TEXT ,-- 比赛描述
	role INTEGER ,-- 角色，记录数据的角色，比如1记录A队的数据
	start_time TEXT ,-- 比赛开始时间
	location TEXT  -- 比赛地点
);

-- 比赛时间表（记录比赛时间点，中间暂停等）
CREATE TABLE tb_game_time (
	_id INTEGER  PRIMARY KEY AUTOINCREMENT ,
	g_id LONG ,-- 比赛id
	t_id LONG ,-- 队伍id
	suspend_time TEXT ,-- 暂停时间
	continue_time TEXT  -- 继续时间
 );
 
 -- 队伍表
CREATE TABLE tb_group (
	_id LONG  PRIMARY KEY ,
	g_id LONG ,-- 隶属的比赛id
	name TEXT ,-- 队名
	slogan TEXT  -- 队标语
 );
 
 -- 球员表
CREATE TABLE tb_member (
	_id LONG  PRIMARY KEY ,
	g_id LONG ,-- 隶属比赛id
	t_id LONG ,-- 隶属队伍id
	name TEXT ,-- 球员姓名
	num TEXT ,-- 球员球衣号
	site TEXT ,-- 位置（如：前锋）
	is_leader INTEGER  -- 队长（比如1：队长）
);

-- 球员上场时间表（记录球员上场次数，以及总上场时间）
CREATE TABLE tb_playing_time (
	_id INTEGER  PRIMARY KEY AUTOINCREMENT ,
	g_id LONG ,-- 隶属比赛id
	t_id LONG ,-- 隶属队伍id
	m_id LONG ,-- 隶属球员id
	start_time TEXT ,-- 开始上场时间
	end_time TEXT  -- 结束上场时间
 );
 
 -- 记录表（球员行为记录表，比如8号二分不中）
CREATE TABLE tb_record ( 
	_id INTEGER  PRIMARY KEY AUTOINCREMENT ,
	g_id LONG ,-- 隶属比赛id
	t_id LONG ,-- 隶属队伍id
	m_id LONG ,-- 隶属球员id
	a_id LONG ,-- 动作行为id
	show_time TEXT ,-- 发生时间
	create_time TEXT ,-- 记录时间
	remark TEXT  -- 备注
);

COMMIT;
