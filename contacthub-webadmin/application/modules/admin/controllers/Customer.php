<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Customer extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_affiliate');
        $this->load->model('admin/api/Api_contact');
        $this->load->model('admin/api/Api_city');
        $this->load->model('admin/api/Api_credit');
        $this->load->model('admin/api/Api_customer');
        $this->load->model('admin/api/Api_greeting');
        $this->load->model('admin/api/Api_interest');
        $this->load->model('admin/api/Api_profesi');
        $this->load->model('admin/api/Api_businesstype');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_customer");
		$this->assets 	= array('assets_index');
        $this->content  = "customer/index";
        
        $city = $this->Api_city->getAll();
        $data['City'] = ['' => 'Semua Kota'];
        foreach ($city->result as $item) {
            $data['City'][$item->CityId] = $item->City;
        }
        $params = [
            'action_home' => 'dashboard',
            'action_add' => 'customer/add',
            'data' => $data,
            'breadcrumb_active' => lang('label_customer')
        ];
		$this->template($params);
    }
    public function view($id){
        $this->title 	= lang('label_view_customer');
		$this->assets 	= array('assets_form_view');
		$this->content  = "customer/view";
        $id = urldecode($id);
        $data['Result'] = $this->Api_customer->getAll(['filter' => ['CustomerId' => $id]]);
		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('customer');
        }
        
        $data['Customer'] = $data['Result']->result->data[0];
        $data['Upline'] = $this->Api_customer->getUpline($data['Customer']->UserId)->result;
        $data['Credit'] = $this->Api_credit->get_balance_datatables([
            'numberPage' => 1,
            'page' => 1,
            'filter' => [
                'UserId' => $data["Customer"]->UserId
            ]
        ])->result->data;
        if (count($data['Credit']) > 0){
            $data['Credit'] = $data['Credit'][0];
        }
        if (count($data['Upline']) > 0){
            $data['Upline'] = $data['Upline'][0];
            if (@$data['Upline']->SaveMyContactBy->Type == 'other'){
                if ($data['Upline']->SaveMyContactBy->OtherKey == 'Religion') $data['Upline']->SaveMyContactBy->Type = 'Agama';
                if ($data['Upline']->SaveMyContactBy->OtherKey == 'Gender') $data['Upline']->SaveMyContactBy->Type = 'Jenis Kelamin';
                if ($data['Upline']->SaveMyContactBy->OtherKey == 'CityId') $data['Upline']->SaveMyContactBy->Type = 'Kota';
            }else if(@$data['Upline']->SaveMyContactBy->Type == 'interest'){
                $data['Upline']->SaveMyContactBy->Type = 'Ketertarikan';
            }else if(@$data['Upline']->SaveMyContactBy->Type == 'random'){
                $data['Upline']->SaveMyContactBy->Type = 'Acak';
            }
            if (@$data['Upline']->SaveContactFriendBy->Type == 'other'){
                if ($data['Upline']->SaveContactFriendBy->OtherKey == 'Religion') $data['Upline']->SaveContactFriendBy->Type = 'Agama';
                if ($data['Upline']->SaveContactFriendBy->OtherKey == 'Gender') $data['Upline']->SaveContactFriendBy->Type = 'Jenis Kelamin';
                if ($data['Upline']->SaveContactFriendBy->OtherKey == 'CityId') $data['Upline']->SaveContactFriendBy->Type = 'Kota';
            }else if (@$data['Upline']->SaveContactFriendBy->Type == 'interest'){
                $data['Upline']->SaveContactFriendBy->Type = 'Ketertarikan';
            }else if (@$data['Upline']->SaveContactFriendBy->Type == 'random'){
                $data['Upline']->SaveContactFriendBy->Type = 'Acak';
            }
        }
        if (@$data['Customer']->SaveMyContactBy->Type == 'other'){
            if ($data['Customer']->SaveMyContactBy->OtherKey == 'Religion') $data['Customer']->SaveMyContactBy->Type = 'Agama';
            if ($data['Customer']->SaveMyContactBy->OtherKey == 'Gender') $data['Customer']->SaveMyContactBy->Type = 'Jenis Kelamin';
            if ($data['Customer']->SaveMyContactBy->OtherKey == 'CityId') $data['Customer']->SaveMyContactBy->Type = 'Kota';
        }else if(@$data['Customer']->SaveMyContactBy->Type == 'interest'){
            $data['Customer']->SaveMyContactBy->Type = 'Ketertarikan';
        }else if(@$data['Customer']->SaveMyContactBy->Type == 'random'){
            $data['Customer']->SaveMyContactBy->Type = 'Acak';
        }
        if (@$data['Customer']->SaveContactFriendBy->Type == 'other'){
            if ($data['Customer']->SaveContactFriendBy->OtherKey == 'Religion') $data['Customer']->SaveContactFriendBy->Type = 'Agama';
            if ($data['Customer']->SaveContactFriendBy->OtherKey == 'Gender') $data['Customer']->SaveContactFriendBy->Type = 'Jenis Kelamin';
            if ($data['Customer']->SaveContactFriendBy->OtherKey == 'CityId') $data['Customer']->SaveContactFriendBy->Type = 'Kota';
        }else if (@$data['Customer']->SaveContactFriendBy->Type == 'interest'){
            $data['Customer']->SaveContactFriendBy->Type = 'Ketertarikan';
        }else if (@$data['Customer']->SaveContactFriendBy->Type == 'random'){
            $data['Customer']->SaveContactFriendBy->Type = 'Acak';
        }

        $param = array(
            'action_home' => 'dashboard',
            'data' => $data,
            'link_upline' => '#',
            'breadcrumb_active' => lang('label_customer')
		);
		$this->template($param);
    }
    public function edit($id){
		$this->title 	= lang('label_edit_customer');
		$this->assets 	= array('assets_form');
		$this->content  = "customer/edit";
        $id = urldecode($id);
        $data['Result'] = $this->Api_customer->getAll(['filter' => ['CustomerId' => $id]]);
		if (empty($data['Result']->result) || (is_array($data['Result']->result) && count($data['Result']->result) == 0)){
			create_alert(['type'=>'danger','message'=> $data['Result']->message]);
			redirect('customer');
        }
        $greeting = $this->Api_greeting->getAll();
        $business_type = $this->Api_businesstype->getAll();
        $interest = $this->Api_interest->getAll();
        $profession = $this->Api_profesi->getAll();
        $city = $this->Api_city->getAll();
        $data['RelationshipStatus'] = ['' => 'Pilih Status Hubungan','Menikah'=>'Menikah','Belum Menikah' => 'Belum Menikah','Cerai' => 'Cerai'];
        $data['Gender'] = ['' => 'Pilih Jenis Kelamin','Laki-laki'=>'Laki-laki','Perempuan' => 'Perempuan'];
        $data['Religion'] = ['' => 'Pilih Agama','Islam'=>'Islam','Kristen Katolik' => 'Kristen Katolik','Kristen Protestan'=>'Kristen Protestan','Hindu'=>'Hindu','Budha'=>'Budha'];
        $data['Profession'] = ['' => 'Pilih Profesi'];
        $data['BusinessType'] = ['' => 'Pilih Tipe Bisnis'];
        $data['Greeting'] = ['' => 'Pilih Sapaan'];
        $data['Interest'] = [];
        $data['IsComplete'] = [false => 'Tidak',true => 'Ya'];
        $data['AllowedShareProfile'] = [false => 'Tidak',true => 'Ya'];
        $data['SaveContactFriendBy'] = ['random'=>'Random','interest' => 'Ketertarikan','Religion' => 'Agama','Gender'=>'Jenis Kelamin','CityId'=>'Kota'] ;
        $data['SaveMyContactBy'] = ['random'=>'Random','interest' => 'Ketertarikan','Religion' => 'Agama','Gender'=>'Jenis Kelamin','CityId'=>'Kota'];
        
        $data['City'] = [
            null => 'Pilih Kota'
        ];
        foreach ($city->result as $item) {
            $data['City'][$item->Province->Province][$item->CityId] = $item->City;
        }
        
        foreach ($profession->result as $item) {
            $data['Profession'][$item->ProfessionId] = $item->ProfessionId;
        }
        foreach ($greeting->result as $item) {
            $data['Greeting'][$item->GreetingId] = $item->GreetingId;
        }

        foreach ($business_type->result as $item) {
            $data['BusinessType'][$item->BusinessTypeId] = $item->BusinessTypeId;
        }
        foreach ($interest->result as $item) {
            $data['Interest'][$item->InterestId] = $item->InterestId;
        }


        $data['Customer'] = $data['Result']->result->data[0];
        if (@$data['Customer']->SaveMyContactBy->Type == 'other'){
            $data['Customer']->SaveMyContactBy->Type = $data['Customer']->SaveMyContactBy->OtherKey;
        }
        if (@$data['Customer']->SaveContactFriendBy->Type == 'other'){
            $data['Customer']->SaveContactFriendBy->Type = $data['Customer']->SaveContactFriendBy->OtherKey;
        }
        $param = array(
            'action_home' => 'dashboard',
            'action_save' => 'customer/update/'.$id,
            'action_cancel' => 'customer',
            'data' => $data,
            'breadcrumb_active' => lang('label_customer')
		);
		$this->template($param);
	}

    public function update($id){
        $data = [];
        $data['CustomerId']                 = $id;
        $data['Greeting']                   = $this->input->post('Greeting',TRUE);
        $data['Name']                       = $this->input->post('Name',TRUE);
        $data['Nickname']                   = $this->input->post('Nickname',TRUE);
        $data['WhatsApp']                   = $this->input->post('WhatsApp',TRUE);
        $data['Address']                    = $this->input->post('Address',TRUE);
        $data['Religion']                   = $this->input->post('Religion',TRUE);
        $data['Gender']                     = $this->input->post('Gender',TRUE);
        $data['BusinessName']               = $this->input->post('BusinessName',TRUE);
        $data['Email']                      = $this->input->post('Email',TRUE);
        $data['DateOfBirth']                = $this->input->post('DateOfBirth',TRUE);
        $data['BusinessTypeId']             = $this->input->post('BusinessTypeId',TRUE);
        $data['ProfessionId']               = $this->input->post('ProfessionId',TRUE);
        $data['SaveContactFriendBy']        = $this->input->post('SaveContactFriendBy',TRUE);
        $data['SaveMyContactBy']            = $this->input->post('SaveMyContactBy',TRUE);
        $data['RelationshipStatus']         = $this->input->post('RelationshipStatus',TRUE);
        $data['InterestId']                 = $this->input->post('InterestId[]',TRUE);
        $data['Hoby']                       = $this->input->post('Hoby',TRUE);
        $data['Facebook']                   = $this->input->post('Facebook',TRUE);
        $data['Instagram']                  = $this->input->post('Instagram',TRUE);
        $data['LimitSaveContactFriendDay']  = $this->input->post('LimitSaveContactFriendDay',TRUE);
        $data['LimitSaveMyContactDay']      = $this->input->post('LimitSaveMyContactDay',TRUE);
        $data['Product']                    = $this->input->post('Product',TRUE);
        $data['Website']                    = $this->input->post('Website',TRUE);
        $data['Password']                   = $this->input->post('Password',TRUE);
        $data['AllowedShareProfile']        = $this->input->post('AllowedShareProfile',TRUE);
        $data['IsComplete']                 = $this->input->post('IsComplete',TRUE);
        $data['SaveContactFriendInterest']  = $this->input->post('SaveContactFriendInterest[]');
        $data['SaveMyContactInterest']      = $this->input->post('SaveMyContactInterest[]');
        $data['CityId']                     = $this->input->post('CityId',TRUE);
            
        if (empty($data['LimitSaveContactFriendDay']) or trim($data['LimitSaveContactFriendDay']) == ""){
            $data['LimitSaveContactFriendDay'] = 0;
        }else{
            $data['LimitSaveContactFriendDay'] = (int) $data['LimitSaveContactFriendDay'];
        }
        if (empty($data['LimitSaveMyContactDay']) or trim($data['LimitSaveMyContactDay']) == ""){
            $data['LimitSaveMyContactDay'] = 0;
        }else{
            $data['LimitSaveMyContactDay'] = (int) $data['LimitSaveMyContactDay'];
        }
        if ($data['IsComplete'] == 1){
            $data['IsComplete'] = true;
        }else{
            $data['IsComplete'] = false;
        }
        if ($data['AllowedShareProfile'] == 1){
            $data['AllowedShareProfile'] = true;
        }else{
            $data['AllowedShareProfile'] = false;
        }

        if ($data['SaveContactFriendBy'] == 'Religion'){
            $data['SaveContactFriendBy'] = 'other';
            $data['SaveContactFriendOtherKey'] = 'Religion';
            $data['SaveContactFriendOtherValue'] = $this->input->post('SaveContactFriendReligion',TRUE);
        }else if ($data['SaveContactFriendBy'] == 'Gender'){
            $data['SaveContactFriendBy'] = 'other';
            $data['SaveContactFriendOtherKey'] = 'Gender';
            $data['SaveContactFriendOtherValue'] = $this->input->post('SaveContactFriendGender',TRUE);
        }else if ($data['SaveContactFriendBy'] == 'CityId'){
            $data['SaveContactFriendBy'] = 'other';
            $data['SaveContactFriendOtherKey'] = 'CityId';
            $data['SaveContactFriendOtherValue'] = $this->input->post('SaveContactFriendCityId',TRUE);
        }else{
            $data['SaveContactFriendOtherKey'] = null;
            $data['SaveContactFriendOtherValue'] = null;
        }

        if ($data['SaveMyContactBy'] == 'Religion'){
            $data['SaveMyContactBy'] = 'other';
            $data['SaveMyContactOtherKey'] = 'Religion';
            $data['SaveMyContactOtherValue'] = $this->input->post('SaveMyContactReligion',TRUE);
        }else if ($data['SaveMyContactBy'] == 'Gender'){
            $data['SaveMyContactBy'] = 'other';
            $data['SaveMyContactOtherKey'] = 'Gender';
            $data['SaveMyContactOtherValue'] = $this->input->post('SaveMyContactGender',TRUE);
        }else if ($data['SaveMyContactBy'] == 'CityId'){
            $data['SaveMyContactBy'] = 'other';
            $data['SaveMyContactOtherKey'] = 'CityId';
            $data['SaveMyContactOtherValue'] = $this->input->post('SaveMyContactCityId',TRUE);
        }else{
            $data['SaveMyContactOtherKey'] = null;
            $data['SaveMyContactOtherValue'] = null;
        }
        if (empty($data['Password']) or trim($data['Password']) == "") {
            unset($data['Password']);
        }
        $save = $this->Api_customer->update($data);
        
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }

        redirect('customer/edit/'.$id);
    }
    public function delete($id){
        $id = rawurldecode($id);
        $data = [];
        $data['CustomerId']       = $id;
        $save = $this->Api_customer->delete($data);
        if($save->status=='OK'){
            create_alert(['type'=>'success','message'=>$save->message]);
        }else{
            create_alert(['type'=>'danger','message'=>$save->message]);
        }
        redirect('customer');
    }
    
    public function ajax_list_save(){
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
        $list = $this->Api_contact->get_list_save($_POST['UserId'],$param);
        // echo json_encode($list);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {

            $no++;
            $row = array();
            $row[] = anchor('customer/view/'.$data->Contact->CustomerId,'Detail',array('class'=>'btn btn-primary btn-xs'));
            $row[] = $data->Contact->Name;
            $row[] = $data->Contact->Email;
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
    public function ajax_list_penghasilan(){
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
        if (isset($_POST['UserId'])){
            if (empty($_POST['UserId']) == false){
                $param['filter']['UserId'] = $_POST['UserId'];
            }
        }
        $list = $this->Api_affiliate->get_datatables_penghasilan_detail($param);
        // echo json_encode($list);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {

            $no++;
            $row = array();
            $row[] = $data->Downline->Name;
            $row[] = $data->UserDownline->Email;
            $row[] = number_format($data->CreditRef->Kredit);
            $row[] = number_format($data->Commission);
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
    public function ajax_list_share(){
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
        $list = $this->Api_contact->get_list_share($_POST['UserId'],$param);
        // echo json_encode($list);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {

            $no++;
            $row = array();
            $row[] = anchor('customer/view/'.$data->Profile->CustomerId,'Detail',array('class'=>'btn btn-primary btn-xs'));
            $row[] = $data->Profile->Name;
            $row[] = $data->Profile->Email;
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
    public function ajax_list_downline(){
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
        if (isset($_POST['UserId'])){
            if (empty($_POST['UserId']) == false){
                $param['filter']['UserId'] = $_POST['UserId'];
            }
        }
        $list = $this->Api_customer->getDownline($param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {

            $no++;
            $row = array();
            $row[] = $data->Downline->Name;
            $row[] = $data->Downline->Gender;
            $row[] = $data->UserDownline->Email;
            $row[] = $data->Downline->WhatsApp;
            $row[] = $data->UserDownline->Status;
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
        if (isset($_POST['CityId'])){
            if (empty($_POST['CityId']) == false){
                $param['filter']['CityId'] = (int) $_POST['CityId'];
            }
        }
        $list = $this->Api_customer->get_datatables($param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = $data->CustomerId;
            $btngroup = create_button_group([
                'content' => lang('label_action'),
                // 'attr'=>array('disabled'=> ($data->is_system) ? true:false),
                'li' => array(
                    ['type'=>'a','content'=>'<i class="fa fa-eye"></i> '.lang('label_view'),'attr'=>array('href'=>'customer/view/'.$id)],
                    ['type'=>'a','content'=>'<i class="fa fa-edit"></i> '.lang('label_edit'),'attr'=>array('href'=>'customer/edit/'.$id)],
                    ['type'=>'a','content'=>'<i class="fa fa-trash"></i> '.lang('label_delete'),'attr'=>array('href'=>base_url('customer/delete/'.$id))]
                )
            ]);

            $no++;
            $row = array();
            $row[] = $btngroup;
            $row[] = $data->Name;
            $row[] = $data->Gender;
            $row[] = $data->User->Email;
            $row[] = $data->WhatsApp;
            $row[] = $data->User->Status;
            $row[] = ($data->IsCompleted === true) ? label_skin(['type'=>'success','text'=>'Ya']) : label_skin(['type'=>'danger','text'=>'Tidak']);
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
