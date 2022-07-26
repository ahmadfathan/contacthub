<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Slider extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_slider');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_slider");
		$this->assets 	= array('assets_index');
        $this->content  = "slider/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'slider/add',
            'breadcrumb_active' => lang('label_slider')
        ];
		$this->template($params);
    }
    public function add(){
		$this->title 	= lang('label_add_slider');
		$this->assets 	= array('assets_form');
        $this->content  = "slider/add";

        $data['Status'] = [
            '' => 'Pilih Status',
            'active' => 'Aktif',
            'inactive' => 'Tidak aktif'
        ];
		$param = array(
            'action_home' => 'dashboard',
            'action_save' => 'slider/create',
            'data' => $data,
            'action_cancel' => 'slider',
            'breadcrumb_active' => lang('label_slider')
		);
		$this->template($param);
    }
    public function edit($id){
		$this->title 	= lang('label_edit_slider');
		$this->assets 	= array('assets_form');
		$this->content  = "slider/edit";
        $data['Result'] = $this->Api_slider->getAll($this->user->UserId,['SliderId' => $id]);
		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('slider');
        }

        $data['Status'] = [
            '' => 'Pilih Status',
            'active' => 'Aktif',
            'inactive' => 'Tidak aktif'
        ];
        $data['Slider'] = $data['Result']->result->data[0];
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'slider/update/'.$id,
            'action_cancel' => 'slider',
            'data' => $data,
            'breadcrumb_active' => lang('label_slider')
		);
		$this->template($param);
	}
    public function create(){
        $data = [];
        $data['Name']       = $this->input->post('Name',TRUE);
        $data['Link']       = $this->input->post('Link',TRUE);
        $data['file']       = $_FILES['file'];
        $data['Status']     = $this->input->post('Status',TRUE);

        if (empty($data['file']['name'])){
            unset($data['file']);
        }
        foreach ($data as $key => $value) {
            if ($key == 'file'){
                $raw_body[] = [
                    'Content-type' => 'multipart/form-data',
                    "name" => $key,
                    "contents" => fopen($value['tmp_name'],'r'),
                    "filename" => $value['name']
                ];
            }else{
                $raw_body[] = [
                    "name" => $key,
                    "contents" => $value
                ];
            }
        }
        $save = $this->Api_slider->insert($this->user->UserId,$raw_body);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('slider/add');
    }

    public function update($id){
        $data = [];
        $data['Name']       = $this->input->post('Name',TRUE);
        $data['Link']       = $this->input->post('Link',TRUE);
        $data['file']       = $_FILES['file'];

        $data['Status']     = $this->input->post('Status',TRUE);

        if (empty($data['file']['name'])){
            unset($data['file']);
        }
        foreach ($data as $key => $value) {
            if ($key == 'file'){
                $raw_body[] = [
                    'Content-type' => 'multipart/form-data',
                    "name" => $key,
                    "contents" => fopen($value['tmp_name'],'r'),
                    "filename" => $value['name']
                ];
            }else{
                $raw_body[] = [
                    "name" => $key,
                    "contents" => $value
                ];
            }
        }
        $save = $this->Api_slider->update($this->user->UserId,$id,$raw_body);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('slider/edit/'.$id);
    }
    public function delete($id){
        $data = [];
        $data['SliderId']       = $id;
        $save = $this->Api_slider->delete($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('slider');
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
        $list = $this->Api_slider->get_datatables($this->user->UserId,$param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = $data->SliderId;
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'a','content'=>'<i class="fa fa-edit"></i> '.lang('label_edit'),'attr'=>array('href'=>base_url('slider/edit/'.$id))],
                    ['type'=>'a','content'=>'<i class="fa fa-trash"></i> '.lang('label_delete'),'attr'=>array('href'=>base_url('slider/delete/'.$id))]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->Name;
            $row[] = @$data->Link;
            $row[] = ($data->Image == null or empty($data->Image)) ? '' : '<img src="https://sandbox.kontakhub.my.id/uploads/'.$data->Image.'" height="100px"/>';
            $row[] = ($data->Status == 'active') ? label_skin(['type' => 'success','text' => $data->Status]) : label_skin(['type' => 'danger','text' => $data->Status]) ;
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
