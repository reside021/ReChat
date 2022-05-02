using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class UpdateRangUser
    {
        public string dialog_id { get; set; }
        public string authorId { get; set; }
        public string tagUser { get; set; }
        public int dataUpdated { get; set; }
    }
}
