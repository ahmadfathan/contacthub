<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Article extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_article');
        $this->load->model('admin/api/Api_interest');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_article");
		$this->assets 	= array('assets_index');
        $this->content  = "article/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'article/add',
            'breadcrumb_active' => lang('label_article')
        ];
		$this->template($params);
    }
    public function add(){
		$this->title 	= lang('label_add_article');
		$this->assets 	= array('assets_form');
        $this->content  = "article/add";

        $interest = $this->Api_interest->getAll();
        
        $data = [];
        $data['Category'] = [null => 'Uncategory'];
        foreach ($interest->result as $item) {
            $data['Category'][$item->InterestId] = $item->InterestId;
        }
        $data['Status'] = [
            '' => 'Pilih Status',
            'publish' => 'Publish',
            'draft' => 'Draft',
            'unpublish' => 'Unpublish',
        ];
		$param = array(
            'action_home' => 'dashboard',
            'action_save' => 'article/create',
            'data' => $data,
            'action_cancel' => 'article',
            'breadcrumb_active' => lang('label_article')
		);
		$this->template($param);
    }
    public function edit($id){
		$this->title 	= lang('label_edit_article');
		$this->assets 	= array('assets_form');
		$this->content  = "article/edit";
        $data['Result'] = $this->Api_article->getAll($this->user->UserId,['filter' => ['ArticleId' => $id]]);
		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('article');
        }

        $interest = $this->Api_interest->getAll();
        
        $data['Category'] = [null => 'Uncategory'];
        foreach ($interest->result as $item) {
            $data['Category'][$item->InterestId] = $item->InterestId;
        }
        $data['Status'] = [
            '' => 'Pilih Status',
            'publish' => 'Publish',
            'draft' => 'Draft',
            'unpublish' => 'Unpublish',
        ];
        $data['Article'] = $data['Result']->result->data[0];
        if (is_array($data['Article']->Tag)){
            $tag = "";
            for ($i=0; $i < count($data['Article']->Tag); $i++) { 
                $tag .= $data['Article']->Tag[$i].';';
            }
            $data['Article']->Tag = $tag;
        }
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'article/update/'.$id,
            'action_cancel' => 'article',
            'data' => $data,
            'breadcrumb_active' => lang('label_article')
		);
		$this->template($param);
	}
    public function create(){
        $data = [];
        $data['Title']      = $this->input->post('Title',TRUE);
        $data['Description']= $this->input->post('Description',TRUE);
        $data['Tag']        = $this->input->post('Tag',TRUE);
        $data['file']       = $_FILES['file'];

        $data['Category']   = $this->input->post('Category',TRUE);
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
        $save = $this->Api_article->insert($this->user->UserId,$raw_body);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('article/add');
    }

    public function update($id){
        $data = [];
        $data['ArticleId']  = $id;
        $data['Title']      = $this->input->post('Title',TRUE);
        $data['Description']= $this->input->post('Description',TRUE);
        $data['Tag']        = $this->input->post('Tag',TRUE);
        $data['file']       = $_FILES['file'];

        $data['Category']   = $this->input->post('Category',TRUE);
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
        $save = $this->Api_article->update($this->user->UserId,$raw_body);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('article/edit/'.$id);
    }
    public function delete($id){
        $data = [];
        $data['ArticleId']       = $id;
        $save = $this->Api_article->delete($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('article');
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
        $list = $this->Api_article->get_datatables($this->user->UserId,$param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = $data->ArticleId;
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'a','content'=>'<i class="fa fa-edit"></i> '.lang('label_edit'),'attr'=>array('href'=>base_url('article/edit/'.$id))],
                    ['type'=>'a','content'=>'<i class="fa fa-trash"></i> '.lang('label_delete'),'attr'=>array('href'=>base_url('article/delete/'.$id))]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->CreatedAt;
            $row[] = $data->UpdatedAt;
            $row[] = $data->Title;
            $row[] = (strlen(html_entity_decode($data->Description)) > 100 ? substr(html_entity_decode($data->Description),0,100) : html_entity_decode($data->Description));
            $row[] = $data->Status;
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
