<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Interest extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_interest');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_interest");
		$this->assets 	= array('assets_index');
        $this->content  = "interest/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'interest/add',
            'breadcrumb_active' => lang('label_interest')
        ];
		$this->template($params);
    }
    public function add(){
		$this->title 	= lang('label_add_interest');
		$this->assets 	= array('assets_form');
        $this->content  = "interest/add";

		$param = array(
            'action_home' => 'dashboard',
            'action_save' => 'interest/create',
            'action_cancel' => 'interest',
            'breadcrumb_active' => lang('label_interest')
		);
		$this->template($param);
    }
    public function edit($id){
		$this->title 	= lang('label_edit_interest');
		$this->assets 	= array('assets_form');
		$this->content  = "interest/edit";
        $id = urldecode($id);
        $data['Result'] = $this->Api_interest->getAll(['InterestId' => $id]);
		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('interest');
        }

        $data['Interest'] = $data['Result']->result[0];
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'interest/update/'.$id,
            'action_cancel' => 'interest',
            'data' => $data,
            'breadcrumb_active' => lang('label_interest')
		);
		$this->template($param);
	}
    public function create(){
        $data = [];
        $data['InterestId'] = $this->input->post('InterestId',TRUE);

        $save = $this->Api_interest->insert($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('interest/add');
    }

    public function update($id){
        $data = [];
        $data['InterestId'] = $this->input->post('InterestId',TRUE);

        $save = $this->Api_interest->insert($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('interest/edit/'.urlencode($data['InterestId']));
    }
    public function delete($id){
        $id = rawurldecode($id);
        $data = [];
        $data['InterestId']       = $id;
        $save = $this->Api_interest->delete($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('interest');
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
        $list = $this->Api_interest->get_datatables($param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = rawurlencode($data->InterestId); // user_id
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'a','content'=>'<i class="fa fa-trash"></i> '.lang('label_delete'),'attr'=>array('href'=>base_url('interest/delete/'.$id))]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->InterestId;
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
