<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Notification extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_notification');
        $this->load->model('admin/api/Api_customer');
        $this->load->model('admin/api/Api_article');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_notification");
		$this->assets 	= array('assets_index');
        $this->content  = "notification/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'notification/add',
            'breadcrumb_active' => lang('label_notification')
        ];
		$this->template($params);
    }
	public function view($id)
	{
		$this->title 	= lang("label_credit");
		$this->assets 	= array('assets_view');
        $this->content  = "credit/view";
        $data_customer = $this->Api_customer->getAll([
            'filter' => [
                "UserId" => $id
            ]
        ]);
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'credit/add',
            'user_name' => @$data_customer->result->data[0]->Name,
            'user_id' => @$data_customer->result->data[0]->UserId,
            'breadcrumb_active' => lang('label_credit')
        ];
		$this->template($params);
    }
    public function add(){
		$this->title 	= lang('label_add_notification');
		$this->assets 	= array('assets_form');
        $this->content  = "notification/add";
        $article = $this->Api_article->getAllNotPaginate($this->user->UserId);
        $data_article = [];
        $data_article[''] = 'Pilih Artikel';
        foreach ($article->result as $item) {
            $data_article[$item->ArticleId] = $item->Title;
        }
        $data = [
            'Article' => $data_article,
            'Type' => [
                'info' => 'Info',
                'link' => 'Link',
                'article' => 'Artikel'
            ]
        ];
		$param = array(
            'action_home' => 'dashboard',
            'action_save' => 'notification/create',
            'data' => $data,
            'action_cancel' => 'notification',
            'breadcrumb_active' => lang('label_notification')
		);
		$this->template($param);
    }
    public function edit($id){
		$this->title 	= lang('label_edit_notification');
		$this->assets 	= array('assets_form');
		$this->content  = "notification/edit";
        $article = $this->Api_article->getAllNotPaginate($this->user->UserId);
        $data_article = [];
        $data_article[''] = 'Pilih Artikel';
        foreach ($article->result as $item) {
            $data_article[$item->ArticleId] = $item->Title;
        }
        $data = [
            'Article' => $data_article,
            'Type' => [
                'info' => 'Info',
                'link' => 'Link',
                'article' => 'Artikel'
            ]
        ];
        $data['Result'] = $this->Api_notification->getAll($this->user->UserId,['NotificationId' => $id]);
		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('notification');
        }

        
        $data['Notification'] = $data['Result']->result[0];
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'notification/update/'.$id,
            'action_cancel' => 'notification',
            'data' => $data,
            'breadcrumb_active' => lang('label_notification')
		);
		$this->template($param);
	}
    public function create(){
        if (isset($_POST['Send'])){
            // Kirim
        }else{
            $data = [];
            $data['Name'] 		    = $this->input->post('Name',TRUE);
            $data['Title'] 	        = $this->input->post('Title',TRUE);
            $data['Body'] 		    = $this->input->post('Body',TRUE);
            $data['Type'] 		    = $this->input->post('Type',TRUE);
            $data['Link'] 		    = $this->input->post('Link',TRUE);
            $data['ArticleId']      = $this->input->post('ArticleId',TRUE);
            if (empty($data['ArticleId']) || $data['ArticleId'] == ""){
                unset($data['ArticleId']);
            }
            $save = $this->Api_notification->insert($this->user->UserId,$data);
            if($save->status=='OK'){
                if (isset($_POST['SaveSend'])){
                    // Kirim
                }
                create_alert(['type'=>'success','message'=>$save->message]);
            }else{
                create_alert(['type'=>'danger','message'=>$save->message]);
            }
        }
        

        redirect('notification/add');
    }

    public function update($id){
        if (isset($_POST['Send'])){
            // Kirim
        }else{
            $data = [];
            $data['ArticleId'] 		= $id;
            $data['Name'] 		    = $this->input->post('Name',TRUE);
            $data['Title'] 	        = $this->input->post('Title',TRUE);
            $data['Body'] 		    = $this->input->post('Body',TRUE);
            $data['Type'] 		    = $this->input->post('Type',TRUE);
            $data['Link'] 		    = $this->input->post('Link',TRUE);
            $data['ArticleId']      = $this->input->post('ArticleId',TRUE);
    
            $save = $this->Api_notification->insert($this->user->UserId,$data);
            if($save->status=='OK'){
                if (isset($_POST['SaveSend'])){
                    // Kirim
                }
                create_alert(['type'=>'success','message'=>$save->message]);
            }else{
                create_alert(['type'=>'danger','message'=>$save->message]);
            }
        }
        
        redirect('notification/edit/'.$id);
    }
    public function delete($id){
        $data = [];
        $data['NotificationId']       = $id;
        $save = $this->Api_notification->delete($this->user->UserId,$data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('notification');
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
        $list = $this->Api_notification->get_datatables($this->user->UserId,$param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = $data->NotificationId; // user_id
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'a','content'=>'<i class="fa fa-edit"></i> '.lang('label_edit'),'attr'=>array('href'=>base_url('notification/edit/'.$id))],
                    ['type'=>'a','content'=>'<i class="fa fa-trash"></i> '.lang('label_delete'),'attr'=>array('href'=>base_url('notification/delete/'.$id))]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->CreatedAt;
            $row[] = $data->Name;
            $row[] = $data->Type;
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
