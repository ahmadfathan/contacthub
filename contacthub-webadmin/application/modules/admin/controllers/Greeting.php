<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Greeting extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_greeting');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_greeting");
		$this->assets 	= array('assets_index');
        $this->content  = "greeting/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'greeting/add',
            'breadcrumb_active' => lang('label_greeting')
        ];
		$this->template($params);
    }
    public function add(){
		$this->title 	= lang('label_add_greeting');
		$this->assets 	= array('assets_form');
        $this->content  = "greeting/add";

		$param = array(
            'action_home' => 'dashboard',
            'action_save' => 'greeting/create',
            'action_cancel' => 'greeting',
            'breadcrumb_active' => lang('label_greeting')
		);
		$this->template($param);
    }
    public function edit($id){
		$this->title 	= lang('label_edit_greeting');
		$this->assets 	= array('assets_form');
		$this->content  = "greeting/edit";
        $id = urldecode($id);
        $data['Result'] = $this->Api_greeting->getAll(['GreetingId' => $id]);
		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('greeting');
        }

        $data['Greeting'] = $data['Result']->result[0];
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'greeting/update/'.$id,
            'action_cancel' => 'greeting',
            'data' => $data,
            'breadcrumb_active' => lang('label_greeting')
		);
		$this->template($param);
	}
    public function create(){
        $data = [];
        $data['GreetingId'] = $this->input->post('GreetingId',TRUE);

        $save = $this->Api_greeting->insert($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('greeting/add');
    }

    public function update($id){
        $data = [];
        $data['GreetingId'] = $this->input->post('GreetingId',TRUE);

        $save = $this->Api_greeting->insert($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('greeting/edit/'.urlencode($data['GreetingId']));
    }
    public function delete($id){
        $id = rawurldecode($id);
        $data = [];
        $data['GreetingId']       = $id;
        $save = $this->Api_greeting->delete($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('greeting');
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
        $list = $this->Api_greeting->get_datatables($param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = rawurlencode($data->GreetingId); // user_id
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'a','content'=>'<i class="fa fa-trash"></i> '.lang('label_delete'),'attr'=>array('href'=>base_url('greeting/delete/'.$id))]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->GreetingId;
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
