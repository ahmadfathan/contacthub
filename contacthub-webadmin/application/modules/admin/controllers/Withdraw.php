<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Withdraw extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_credit');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_withdraw");
		$this->assets 	= array('assets_index');
        $this->content  = "withdraw/index";
        
        $params = [
            'action_home' => 'dashboard',
            'breadcrumb_active' => lang('label_withdraw')
        ];
		$this->template($params);
    }
	public function edit($id){
		$this->title 	= lang('label_edit_withdraw');
		$this->assets 	= array('assets_form');
		$this->content  = "withdraw/edit";

        $data = [
            'Status' => [
                '' => 'Pilih Status',
                'success' => 'Success',
                'pending' => 'Pending',
                'failed' => 'Failed'
            ]
        ];
        $data['Result'] = $this->Api_credit->get_datatables($this->user->UserId,['filter' => ['CreditId' => $id,'Tag' => 'withdraw']]);

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
            'action_save' => 'withdraw/update/'.$id,
            'action_cancel' => 'withdraw',
            'data' => $data,
            'amount' => $amount,
            'breadcrumb_active' => lang('label_withdraw')
		);
		$this->template($param);
	}
    public function update($id){
        $data = [];
        $data['CreditId']       = $id;
        $data['Tag']            = 'withdraw';
        $data['RefNo']          = $this->input->post('RefNo',TRUE);
        $data['Description']    = $this->input->post('Description',TRUE);
        $data['Status'] 		= $this->input->post('Status',TRUE);
        
        $save = $this->Api_credit->update($this->user->UserId,$data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('withdraw');
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
        $param['filter']['Tag'] = 'withdraw';
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
                    ['type'=>'a','content'=>'<i class="fa fa-edit"></i> '.lang('label_edit'),'attr'=>array('href'=>base_url('withdraw/edit/'.$id))]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->CreatedAt;
            $row[] = $data->User->Email;
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
    
    private function labelTable($key){
        switch ($key) {
            case '_id':return lang('label_id');
            case 'Name':return lang('label_nama_role');
            default: return false;
        }
    }
}
