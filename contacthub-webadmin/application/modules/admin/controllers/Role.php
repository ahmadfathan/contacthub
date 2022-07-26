<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Role extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->load->model('admin/api/Api_role');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_data_master_role");
		$this->assets 	= array('assets_index');
        $this->content  = "role/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'role/add',
            'breadcrumb_active' => lang('label_role')
        ];
		$this->template($params);
    }
    public function add(){
		$this->title 	= lang('label_add_role');
		$this->assets 	= array('assets_form');
        $this->content  = "role/add";

        $role_access = $this->Api_role->access_role_list();
        $data['role_access'] = [];
        if (count($role_access->result) > 0){
            foreach ($role_access->result as $val) {
                $data['role_access'][$val->_id] = lang('role_access_'.$val->_id);
            }
        }

		$param = array(
            'action_home' => 'dashboard',
            'action_save' => 'role/create',
            'data' => $data,
            'action_cancel' => 'role',
            'breadcrumb_active' => lang('label_role')
		);
		$this->template($param);
    }
    public function edit($id){
		$this->title 	= lang('label_edit_role');
		$this->assets 	= array('assets_form');
		$this->content  = "role/edit";

        $data['Result'] = $this->Api_role->by_id($id);
		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('role');
        }
        $data['Role'] = $data['Result']->result;
        $role_access = $this->Api_role->access_role_list();
        $data['role_access'] = [];
        if (count($role_access->result) > 0){
            foreach ($role_access->result as $val) {
                $data['role_access'][$val->_id] = lang('role_access_'.$val->_id);
            }
        }
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'role/update/'.$data['Role']->RoleId,
            'action_cancel' => 'role',
            'data' => $data,
            'breadcrumb_active' => lang('label_role')
		);
		$this->template($param);
	}
    public function create(){
        $data = [];
        $data['Name'] 		= $this->input->post('Name',TRUE);
        $data['IsAdministrator'] 		= true;
        $data['RoleAccess'] = $this->input->post('RoleAccess[]',TRUE);
        $config = [
            ['field' => 'Name','label' => lang('label_nama'),'rules'	=> rules_validation('no_rules',true)],
        ];
        $this->form_validation->set_rules($config);
        if ($this->form_validation->run() == FALSE){
            create_alert(['type'=>'danger','message'=>validation_errors()]);
        }else{
            $save = $this->Api_role->insert($data);
            if($save->status=='OK'){
                create_alert(['type'=>'success','message'=>$save->message]);
            }else{
                create_alert(['type'=>'danger','message'=>$save->message]);
            }

        }
        redirect('role/add');
    }
    public function update($id){
        $data = [];
        $data['Name'] 	= $this->input->post('Name',TRUE);
        $data['RoleAccess'] = $this->input->post('RoleAccess[]',TRUE);
        $config = [
            ['field' => 'Name','label' => lang('label_nama'),'rules'	=> rules_validation('no_rules',true)],
        ];
        $this->form_validation->set_rules($config);
        if ($this->form_validation->run() == FALSE){
            create_alert(['type'=>'danger','message'=>validation_errors()]);
        }else{
            
            $save = $this->Api_role->update($id,$data);
            if($save->status=='OK'){
                create_alert(['type'=>'success','message'=>$save->message]);
            }else{
                create_alert(['type'=>'danger','message'=>$save->message]);
            }

        }
        redirect('role/edit/'.$id);
    }
    public function delete($id){
        $save = $this->Api_role->delete($id);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('role');
    }
    public function ajax_list(){
        $param = [];
        $url_delete = 'role/delete/';
        $url_edit = 'role/edit/';

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
        $list = $this->Api_role->get_datatables($param);
        $data = array();

        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = $data->RoleId;
            if ($id == 2) continue;
            $action_hapus = base_url($url_delete.$id);
            $json_hapus = htmlspecialchars(json_encode(['id'=>$id,'description'=>$data->Name]),ENT_QUOTES);
            $json_view = [];
            foreach ($data as $key => $value) {
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
            $row[] = $data->Name;
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
            case 'Name':return lang('label_nama_role');
            default: return false;
        }
    }
}
