<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Businesstype extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_businesstype');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_business_type");
		$this->assets 	= array('assets_index');
        $this->content  = "businesstype/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'businesstype/add',
            'breadcrumb_active' => lang('label_businesstype')
        ];
		$this->template($params);
    }
    public function add(){
		$this->title 	= lang('label_add_business_type');
		$this->assets 	= array('assets_form');
        $this->content  = "businesstype/add";

		$param = array(
            'action_home' => 'dashboard',
            'action_save' => 'businesstype/create',
            'action_cancel' => 'businesstype',
            'breadcrumb_active' => lang('label_businesstype')
		);
		$this->template($param);
    }
    public function edit($id){
		$this->title 	= lang('label_edit_business_type');
		$this->assets 	= array('assets_form');
		$this->content  = "businesstype/edit";
        $id = urldecode($id);
        $data['Result'] = $this->Api_businesstype->getAll(['BusinessTypeId' => $id]);
		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('businesstype');
        }

        $data['BusinessType'] = $data['Result']->result[0];
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'businesstype/update/'.$id,
            'action_cancel' => 'businesstype',
            'data' => $data,
            'breadcrumb_active' => lang('label_businesstype')
		);
		$this->template($param);
	}
    public function create(){
        $data = [];
        $data['BusinessTypeId'] = $this->input->post('BusinessTypeId',TRUE);

        $save = $this->Api_businesstype->insert($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('businesstype/add');
    }

    public function update($id){
        $data = [];
        $data['BusinessTypeId'] = $this->input->post('BusinessTypeId',TRUE);

        $save = $this->Api_businesstype->insert($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('businesstype/edit/'.urlencode($data['BusinessTypeId']));
    }
    public function delete($id){
        $id = rawurldecode($id);
        $data = [];
        $data['BusinessTypeId']       = $id;
        $save = $this->Api_businesstype->delete($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('businesstype');
    }
    
    public function ajax_list(){
        $param = [];

        if ($_POST['search']['value']){
            $param['keyword'] = $_POST['search']['value'];
        }
        $param['numberPage'] = (int) $_POST['length'];
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
        $list = $this->Api_businesstype->get_datatables($param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = rawurlencode($data->BusinessTypeId); // user_id
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'a','content'=>'<i class="fa fa-trash"></i> '.lang('label_delete'),'attr'=>array('href'=>base_url('businesstype/delete/'.$id))]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->BusinessTypeId;
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
