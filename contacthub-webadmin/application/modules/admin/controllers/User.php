<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class User extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->load->model('admin/api/Api_user');
        $this->load->model('admin/api/Api_role');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_pengguna");
		$this->assets 	= array('assets_index');
        $this->content  = "pengguna/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'pengguna/add',
            'breadcrumb_active' => lang('label_pengguna')
        ];
		$this->template($params);
    }
    public function add(){
		$this->title 	= lang('label_add_pengguna');
		$this->assets 	= array('assets_form');
        $this->content  = "pengguna/add";
        $role = $this->Api_role->getAll();  
        $status_user = $this->Api_user->getListStatus();  
        $data['role'] = [
            '' => lang('label_select')
        ];
        $data['status'] = [
            '' => lang('label_select'),
            'active' => 'Active',
            'inactive' => 'Inactive',
            'blocked' => 'Blocked'
        ];
        if(count($role->result)>0){
            foreach ($role->result as $val) {
                if ($val->IsAdministrator == false || $val->RoleId == 2){ //customer
                    continue;
                }
                $data['role'][$val->RoleId] = $val->Name;
            }
        }
		$param = array(
            'action_home' => 'dashboard',
            'action_save' => 'pengguna/create',
            'action_cancel' => 'pengguna',
            'data' => $data,
            'breadcrumb_active' => lang('label_pengguna')
		);
		$this->template($param);
    }
    public function edit($id){
		$this->title 	= lang('label_edit_pengguna');
		$this->assets 	= array('assets_form');
		$this->content  = "pengguna/edit";

        $data['result'] = $this->Api_user->by_id($id)->result;
		if (empty($data['result']) || (is_array($data['result']) && count($data['result']) == 0)){
			create_alert(['type'=>'danger','message'=> lang('attention_not_found')]);
			redirect('pengguna');
        }
        $data['user'] = $data['result'];
        $role = $this->Api_role->getAll();  
        $status_user = $this->Api_user->getListStatus();  
        $data['role'] = [
            '' => lang('label_select')
        ];
        $data['status'] = [
            '' => lang('label_select'),
            'active' => 'Active',
            'inactive' => 'Inactive',
            'blocked' => 'Blocked'
        ];
        if(count($role->result)>0){
            foreach ($role->result as $val) {
                if ($val->IsAdministrator == false || $val->RoleId == 2){ //customer
                    continue;
                }
                $data['role'][$val->RoleId] = $val->Name;
            }
        }
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'pengguna/update/'.$id,
            'action_cancel' => 'pengguna',
            'data' => $data,
            'breadcrumb_active' => lang('label_pengguna')
		);
		$this->template($param);
	}
    public function create(){
        $data = [];
        $data['Username'] 		= $this->input->post('Username',TRUE);
        $data['Nickname'] 		= $this->input->post('Nickname',TRUE);
        $data['Email'] 		    = $this->input->post('Email',TRUE);
        $data['Password'] 		= $this->input->post('Password',TRUE);
        $data['RoleId'] 		= $this->input->post('RoleId',TRUE);
        $data['Status'] 		= $this->input->post('Status',TRUE);
        $config = [
            ['field' => 'Username','label' => lang('label_username'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'Nickname','label' => lang('label_nickname'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'Email','label' => lang('label_email'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'Password','label' => lang('label_password'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'RoleId','label' => lang('label_role'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'Status','label' => lang('label_status'),'rules'	=> rules_validation('no_rules',true)],
        ];
        $this->form_validation->set_rules($config);
        if ($this->form_validation->run() == FALSE){
            create_alert(['type'=>'danger','message'=>validation_errors()]);
        }else{
            
            $save = $this->Api_user->insert($data);
            if($save->status=='OK'){
                create_alert(['type'=>'success','message'=>lang('notif_save_success')]);
            }else{
                create_alert(['type'=>'danger','message'=>$save->message]);
            }

        }
        redirect('pengguna/add');
    }
    public function update($id){
        $data = [];
        $data['Username'] 		= $this->input->post('Username',TRUE);
        $data['Nickname'] 		= $this->input->post('Nickname',TRUE);
        $data['Email'] 		    = $this->input->post('Email',TRUE);
        $data['Password'] 		= $this->input->post('Password',TRUE);
        $data['RoleId'] 		    = $this->input->post('RoleId',TRUE);
        $data['Status'] 		= $this->input->post('Status',TRUE);
        $config = [
            ['field' => 'Username','label' => lang('label_username'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'Nickname','label' => lang('label_nickname'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'Email','label' => lang('label_email'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'RoleId','label' => lang('label_role'),'rules'	=> rules_validation('no_rules',true)],
            ['field' => 'Status','label' => lang('label_status'),'rules'	=> rules_validation('no_rules',true)],
        ];
        $this->form_validation->set_rules($config);
        if ($this->form_validation->run() == FALSE){
            create_alert(['type'=>'danger','message'=>validation_errors()]);
        }else{
            if (empty(trim($data['Password']))){
                unset($data['Password']);
            }
            $save = $this->Api_user->update($id,$data);
            if($save->status=='OK'){
                create_alert(['type'=>'success','message'=>$save->message]);
            }else{
                create_alert(['type'=>'danger','message'=>$save->message]);
            }

        }
        redirect('pengguna/edit/'.$id);
    }
    public function delete($id){
        if ($this->user->UserId==$id){
            create_alert(['type'=>'danger','message'=>lang('notif_delete_failed')]);
        }else{
            $save = $this->Api_user->delete($id);
            if($save->status=='OK'){
                create_alert(['type'=>'success','message'=>lang('notif_delete_success')]);
            }else{
                create_alert(['type'=>'danger','message'=>$save->message]);
            }
        }
        
        redirect('pengguna');
    }
    public function ajax_list(){
        $param = [];
        $url_delete = 'pengguna/delete/';
        $url_edit = 'pengguna/edit/';

        if ($_POST['search']['value']){
            $param['keyword'] = $_POST['search']['value'];
        }
        $param['per_page'] = (int) $_POST['length'];
        $param['page'] = (int) $_POST['start'];
        if ($param['page']==0){
            $param['page'] = 1;
        }else{
            $param['page'] = $param['page'] / $param['numberPage'] + 1 ;
        }
        if (isset($_POST['order'])){
            $param['order_column'] = $_POST['order']['0']['column'];
            $param['order_dir'] = $_POST['order']['0']['dir'];
        }
        $list = $this->Api_user->get_datatables($param);
        $data = array();

        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            if($data->RoleId == 2 || $data->Role->IsAdministrator == false){ // Customer
                continue;
            }
            $id = $data->UserId;
            $action_hapus = base_url($url_delete.$id);
            $json_hapus = htmlspecialchars(json_encode(['id'=>$id,'description'=>$data->Username]),ENT_QUOTES);
            $json_view = [];
            foreach ($data as $key => $value) {
                if (is_object($value)){
                    if ($key=='Role'){
                        $value = @$value->Name;
                    }elseif ($key=='Status'){
                        $value = $value->Description;
                    }
                }
                if (($key = $this->labelTable($key)) == false) continue;
                
                $value = htmlspecialchars($value,ENT_QUOTES);
                $json_view[] = ['label'=>$key,'value'=>$value];
            }
            $json_view = htmlspecialchars(json_encode($json_view),ENT_QUOTES);
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'button','content'=>'<i class="fa fa-eye"></i> '.lang('label_view'),'attr'=>array('onclick'=>'modal_view(\''.$json_view.'\')')],
                    ['type'=>'divider'],
                    ['type'=>'a','content'=>'<i class="fa fa-edit"></i> '.lang('label_edit'),'attr'=>array('href'=>$url_edit.$id)],
                    ['type'=>'button','content'=>'<i class="fa fa-trash"></i> '.lang('label_hapus'),'attr'=>array('onclick'=>'hapusItem(\''.$json_hapus.'\',\''.$action_hapus.'\')')]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->Username;
            $row[] = $data->Nickname;
            $row[] = @$data->Email;
            $row[] = isset($data->Role->Name) ? $data->Role->Name : '';
            $row[] = isset($data->Status) ? $data->Status : '' ;
            $dataTables[] = $row;
        }

        $output = array(
            "draw" => $_POST['draw'],
            "recordsTotal" => $list->result->total,
            "recordsFiltered" => $list->result->total,
            "data" => $dataTables,
        );
        //output to json format
        echo json_encode($output);
    }
    private function labelTable($key){
        switch ($key) {
            case '_id':return lang('label_id');
            case 'Username':return lang('label_username');
            case 'Nickname':return lang('label_nickname');
            case 'Email':return lang('label_email');
            case 'Role':return lang('label_nama_role');
            case 'Status':return lang('label_status');
            default: return false;
        }
    }
}
