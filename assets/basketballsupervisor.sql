BEGIN TRANSACTION;

-- ��Ա������Ϊ������Ͷ����
CREATE TABLE tb_action (
	_id INTEGER  PRIMARY KEY AUTOINCREMENT ,
	next_action_id INTEGER ,--��һ��������id���������ݣ�������һ��ѡ��ģ�
	name TEXT ,-- �������ƣ���Ͷ��
	score INTEGER ,-- �÷�
	cancelable INTEGER  -- �Ի����Ƿ����ȡ��
 );
 
 -- ������
CREATE TABLE tb_game (
	_id LONG  PRIMARY KEY ,
	p_id LONG ,-- ƽ̨id
	name TEXT ,-- ��������
	remark TEXT ,-- ��������
	role INTEGER ,-- ��ɫ����¼���ݵĽ�ɫ������1��¼A�ӵ�����
	start_time TEXT ,-- ������ʼʱ��
	location TEXT  -- �����ص�
);

-- ����ʱ�����¼����ʱ��㣬�м���ͣ�ȣ�
CREATE TABLE tb_game_time (
	_id INTEGER  PRIMARY KEY AUTOINCREMENT ,
	g_id LONG ,-- ����id
	t_id LONG ,-- ����id
	suspend_time TEXT ,-- ��ͣʱ��
	continue_time TEXT  -- ����ʱ��
 );
 
 -- �����
CREATE TABLE tb_group (
	_id LONG  PRIMARY KEY ,
	g_id LONG ,-- �����ı���id
	name TEXT ,-- ����
	slogan TEXT  -- �ӱ���
 );
 
 -- ��Ա��
CREATE TABLE tb_member (
	_id LONG  PRIMARY KEY ,
	g_id LONG ,-- ��������id
	t_id LONG ,-- ��������id
	name TEXT ,-- ��Ա����
	num TEXT ,-- ��Ա���º�
	site TEXT ,-- λ�ã��磺ǰ�棩
	is_leader INTEGER  -- �ӳ�������1���ӳ���
);

-- ��Ա�ϳ�ʱ�����¼��Ա�ϳ��������Լ����ϳ�ʱ�䣩
CREATE TABLE tb_playing_time (
	_id INTEGER  PRIMARY KEY AUTOINCREMENT ,
	g_id LONG ,-- ��������id
	t_id LONG ,-- ��������id
	m_id LONG ,-- ������Աid
	start_time TEXT ,-- ��ʼ�ϳ�ʱ��
	end_time TEXT  -- �����ϳ�ʱ��
 );
 
 -- ��¼����Ա��Ϊ��¼������8�Ŷ��ֲ��У�
CREATE TABLE tb_record ( 
	_id INTEGER  PRIMARY KEY AUTOINCREMENT ,
	g_id LONG ,-- ��������id
	t_id LONG ,-- ��������id
	m_id LONG ,-- ������Աid
	a_id LONG ,-- ������Ϊid
	show_time TEXT ,-- ����ʱ��
	create_time TEXT ,-- ��¼ʱ��
	remark TEXT  -- ��ע
);

COMMIT;
