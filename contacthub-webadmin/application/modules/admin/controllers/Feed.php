<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Feed extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_feed');
        $this->load->model('admin/api/Api_interest');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_ads_feed");
		$this->assets 	= array('assets_index');
        $this->content  = "feed/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'feed/add',
            'breadcrumb_active' => lang('label_ads_feed')
        ];
		$this->template($params);
    }
    public function approve($FeedId){
        $user_id =  $this->user->UserId;
        $data = [];
        $data['FeedId']       = $FeedId;
        $data['Status']       = 'publish';
        $save = $this->Api_feed->update_status($user_id,$data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('feed');
    }
    
    public function reject($FeedId){
        $user_id =  $this->user->UserId;
        $data = [];
        $data['FeedId']       = $FeedId;
        $data['Status']       = 'reject';
        $data['Reason']       = $this->input->post('Reason',TRUE);
        $save = $this->Api_feed->update_status($user_id,$data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('feed');
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
        $list = $this->Api_feed->get_datatables($this->user->UserId,$param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = $data->FeedId;

            $action_approve = base_url('feed/approve/'.$id);
            $action_reject = base_url('feed/reject/'.$id);
            $json_review = htmlspecialchars(json_encode([
                'title'=>$data->Title,
                'image'=>'https://sandbox.kontakhub.my.id/uploads/'.$data->Image,
                'action_reject'=>$action_reject,
                'action_approve'=>$action_approve,
                'description'=> htmlspecialchars(str_replace("\n","",$data->Description),ENT_QUOTES),

            ]),ENT_QUOTES);

            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'button','content'=>'<i class="fa fa-eye"></i> '.lang('label_review'),'attr'=>array('onclick'=> 'modalReview(\''.$json_review.'\',\''.$action_approve.'\')')],
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->CreatedAt;
            $row[] = $data->Title;
            $row[] = (strlen(html_entity_decode($data->Description)) > 100 ? substr(html_entity_decode($data->Description),0,100) : html_entity_decode($data->Description));
            $row[] = ($data->Status == 'publish') ? label_skin(['type' => 'success','text' => $data->Status]) : label_skin(['type' => 'danger','text' => $data->Status]) ;
            $row[] = ($data->IsApproved) ? label_skin(['type' => 'success','text' => 'disetujui']) : label_skin(['type' => 'danger','text' => 'belum disetujui']) ;
            $row[] = $data->Reason;
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
