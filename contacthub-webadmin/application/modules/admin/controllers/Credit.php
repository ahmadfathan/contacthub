<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Credit extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_credit');
        $this->load->model('admin/api/Api_customer');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_credit");
		$this->assets 	= array('assets_index');
        $this->content  = "credit/index";
        
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'credit/add',
            'breadcrumb_active' => lang('label_credit')
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
		$this->title 	= lang('label_add_credit');
		$this->assets 	= array('assets_form');
        $this->content  = "credit/add";

        $data_customer = $this->Api_customer->getAllNotPaginate();
        $customer = [];
        $customer[''] = 'Pilih Customer';
        foreach ($data_customer->result as $item) {
            $customer[$item->UserId] = $item->Name.' ('.$item->User->Email.')';
        }
        $data = [
            'Customer' => $customer,
            'Tag' => [
                '' => 'Pilih Jenis Pembayaran',
                'topup' => 'Topup',
                // 'save_contact' => 'Save Contact',
                // 'share_contact' => 'Share Contact',
                // 'fee_affiliate' => 'Fee Affiliate',
                // 'withdraw' => 'Withdraw',
            ],
            'Status' => [
                '' => 'Pilih Status',
                'success' => 'Success',
                'pending' => 'Pending',
                'failed' => 'Failed'
            ]
        ];
		$param = array(
            'action_home' => 'dashboard',
            'action_save' => 'credit/create',
            'data' => $data,
            'action_cancel' => 'credit',
            'breadcrumb_active' => lang('label_credit')
		);
		$this->template($param);
    }
    public function edit($user_id,$id){
		$this->title 	= lang('label_edit_credit');
		$this->assets 	= array('assets_form');
		$this->content  = "credit/edit";

        $data_customer = $this->Api_customer->getAllNotPaginate();
        $customer = [];
        $customer[''] = 'Pilih Customer';
        foreach ($data_customer->result as $item) {
            $customer[$item->UserId] = $item->Name.' ('.$item->User->Email.')';
        }

        $data = [
            'Customer' => $customer,
            'Tag' => [
                '' => 'Pilih Jenis Pembayaran',
                'topup' => 'Topup',
                'save_contact' => 'Save Contact',
                'share_contact' => 'Share Contact',
                'fee_affiliate' => 'Fee Affiliate',
                'withdraw' => 'Withdraw',
            ],
            'Status' => [
                '' => 'Pilih Status',
                'success' => 'Success',
                'pending' => 'Pending',
                'failed' => 'Failed'
            ]
        ];
        $data['Result'] = $this->Api_credit->getAll($user_id,['filter' => ['CreditId' => $id]]);

		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('credit/view/'.$user_id);
        }

        
        $data['Credit'] = $data['Result']->result->data[0];
        if ($data['Credit']->Tag == 'fee_affiliate' || $data['Credit']->Tag == 'topup'){
            $amount = $data['Credit']->Kredit;
        }else{
            $amount = $data['Credit']->Debit;
        }
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'credit/update/'.$user_id.'/'.$id,
            'action_cancel' => 'credit/view/'.$user_id,
            'data' => $data,
            'amount' => $amount,
            'breadcrumb_active' => lang('label_credit')
		);
		$this->template($param);
	}
    public function create(){
        $data = [];
        $data['Tag'] 		    = $this->input->post('Tag',TRUE);
        $data['Description'] 	= $this->input->post('Description',TRUE);
        $data['UserId'] 		= $this->input->post('UserId',TRUE);
        $data['RefNo'] 		    = $this->input->post('RefNo',TRUE);
        $data['Status'] 		= 'success';
        $data['Amount']         = $this->input->post('Amount',TRUE);
        $data['Harga']          = $this->input->post('Harga',TRUE);

        $save = $this->Api_credit->insert($data['UserId'],$data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('credit/add');
    }
    public function update($user_id,$id){
        $data = [];
        $data['CreditId']       = $id;
        $data['Tag'] 		    = $this->input->post('Tag',TRUE);
        $data['Description'] 	= $this->input->post('Description',TRUE);
        $data['UserId'] 		= $this->input->post('UserId',TRUE);
        $data['RefNo'] 		    = $this->input->post('RefNo',TRUE);
        $data['Status'] 		= $this->input->post('Status',TRUE);
        $data['Amount']         = $this->input->post('Amount',TRUE);
        $data['Harga']          = $this->input->post('Harga',TRUE);
        
        $save = $this->Api_credit->update($user_id,$data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('credit/edit/'.$user_id.'/'.$id);
    }
    public function delete($user_id,$id){
        $data = [];
        $data['CreditId']       = $id;
        $save = $this->Api_credit->delete($user_id,$data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('credit/view/'.$user_id);
    }
    public function ajax_list_view(){
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
        $param['filter']['UserId'] = $_POST['UserId'];
        $list = $this->Api_credit->get_datatables($this->user->UserId,$param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = $data->CreditId; // CreditId
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'a','content'=>'<i class="fa fa-eye"></i> '.lang('label_view'),'attr'=>array('href'=>base_url('credit/view/'.$id))],
                    ['type'=>'a','content'=>'<i class="fa fa-edit"></i> '.lang('label_edit'),'attr'=>array('href'=>base_url('credit/edit/'.$data->UserId.'/'.$id))],
                    ['type'=>'a','content'=>'<i class="fa fa-trash"></i> '.lang('label_delete'),'attr'=>array('href'=>base_url('credit/delete/'.$data->UserId.'/'.$id))]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->CreatedAt;
            $row[] = number_format($data->Debit);
            $row[] = number_format($data->Kredit);
            $row[] = $data->Tag;
            $row[] = $data->Description;
            $row[] = $data->RefNo;
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
        $list = $this->Api_credit->get_balance_datatables($param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = $data->_id; // user_id
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'a','content'=>'<i class="fa fa-eye"></i> '.lang('label_view'),'attr'=>array('href'=>base_url('credit/view/'.$id))]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->Customer->Name;
            $row[] = $data->User->Email;
            $row[] = number_format($data->Balance);
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
