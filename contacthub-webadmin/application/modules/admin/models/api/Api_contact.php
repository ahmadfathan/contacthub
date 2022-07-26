<?php
require APPPATH.'vendor/autoload.php';
use GuzzleHttp\Client;

class Api_contact
{
    public function __construct(){
        $this->app =& get_instance();

        $this->client = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API'),
            'timeout'  => 120.0,
        ]);
    }
    public function run_cjob_contact_auto_save(){
        
        try {
            $response   = $this->client->request('GET', '/cjob/contact-save',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                ]
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function get_list_share($user_id,$filter=[]){
        try {
            $response   = $this->client->request('GET', '/contact/list-share',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-Auth-UserId' => $user_id,
                ],
                'json' => $filter 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function get_list_save($user_id,$filter=[]){
        try {
            $response   = $this->client->request('GET', '/contact/list-save',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                    'X-Auth-UserId' => $user_id,
                ],
                'json' => $filter 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    

}
